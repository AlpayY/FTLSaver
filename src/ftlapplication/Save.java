package ftlapplication;

import java.time.format.DateTimeFormatter;
import java.util.TimeZone;
import java.time.Instant;
import java.time.LocalDateTime;
import java.io.File;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class Save {
	File savedir;
	File slotdir;
	long latest = 0;
	String timestamp;
	boolean empty = true;
	
	public Save(final String savepath, final String slotpath) {
		savedir = new File(savepath);
		slotdir = new File(slotpath);
		
		String[] list = slotdir.list((f, d) -> d.endsWith(".sav"));
		try {
			if(list.length > 0) {
				empty = false;
				for(String filename : list) {
					String abs = slotdir.getAbsolutePath() + "/" + filename;
					File file = new File(abs);
					if(file.lastModified() > latest) {
						latest = file.lastModified();
					}
				}
				LocalDateTime dt = LocalDateTime.ofInstant(Instant.ofEpochMilli(latest), TimeZone.getDefault().toZoneId());
				timestamp = getTimestring(dt);
			} else {
				throw new NullPointerException();
			}
		} catch(NullPointerException e) {
			LocalDateTime dt = LocalDateTime.now();
			timestamp = getTimestring(dt);
		}
	}
	
	public boolean checkFolderValidity() throws IllegalArgumentException, RuntimeException {
		if(!savedir.exists()) {
			throw new IllegalArgumentException();
		}
		if(!slotdir.exists()) {
			slotdir.mkdir();
			empty = true;
			return false;
		}
		
		String[] filelist = slotdir.list();
		if(filelist.length < 0 || filelist.length > 3) {
			throw new RuntimeException("Slot directory is already occupied!");
		} else {
			empty = false;
		}
		return true;
	}
	
	public void backup() throws RuntimeException {
		try {
			empty = false;
			LocalDateTime dt = LocalDateTime.now();
			timestamp = getTimestring(dt);
			this.latest = System.currentTimeMillis();
			
			String[] files = savedir.list((f, d) -> d.endsWith(".sav"));
			String inDir = savedir.getAbsolutePath();
			String outDir = slotdir.getAbsolutePath();
			copy(files, inDir, outDir);
			
			FTLSaver.resetButtonTexts();
		} catch(RuntimeException r) {
			throw r;
		} catch(Exception e) {
			throw new RuntimeException(e.getMessage());
		}
	}
	
	public void restore() throws RuntimeException {
		String[] files = slotdir.list((f, d) -> d.endsWith(".sav"));
		String inDir = slotdir.getAbsolutePath();
		String outDir = savedir.getAbsolutePath();
		copy(files, inDir, outDir);
		
		int slot = FTLSaver.getSaves().indexOf(this);
		FTLSaver.setRestoreText("!S" + slot + " RESTORED!");
		new Thread() {
			@Override
			public void run() {
				try {
					sleep(1500);
					FTLSaver.setRestoreText("Last");
				} catch(InterruptedException e) {}
			}
		}.start();
	}
	
	private void copy(String[] files, String inDir, String outDir) {
		for(String file : files) {
			String inputfile = inDir + "/" + file;
			String outputfile = outDir + "/" + file;
			try (
					BufferedInputStream inputStream
			            = new BufferedInputStream(new FileInputStream(inputfile));
			 
			        BufferedOutputStream outputStream
			            = new BufferedOutputStream(new FileOutputStream(outputfile));
			) {
			 
				int byteRead;
			 
			    while ((byteRead = inputStream.read()) != -1) {
			    	outputStream.write(byteRead);
			    }
			} catch(IOException e) {
			    throw new RuntimeException("Could not copy file " + inputfile + " to " + outputfile + "!");
			}
		}
	}
	
	public String getSlotpath() {
		return slotdir.getAbsolutePath();
	}
	
	public String getTimestamp() {
		return timestamp;
	}
	
	public long getTimestampLong() {
		return latest;
	}
	
	private String getTimestring(LocalDateTime dt) {
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss");
		return dtf.format(dt);
	}
	
	public boolean isEmpty() {
		return empty;
	}
}
