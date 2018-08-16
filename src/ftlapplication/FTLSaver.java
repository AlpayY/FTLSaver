package ftlapplication;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;

import java.net.URL;
//import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;

public class FTLSaver {
	
	public static String version = "v1.1";

	private static Save lastSave;
	private static Save nextSave;
	
	private static JButton saveNext;
	private static JButton restoreLast;
	private static JButton[] saveButton;
	private static JButton[] restoreButton;
	
	private static ArrayList<Save> saves;
	private static String savepath = "";
	private static JFrame frame;
	
	private static String windowTitle = "1-up! " + version;
	private static int savecount = 5;
	private static Color aColor = Color.LIGHT_GRAY;
	private static Color bColor = new Color(30, 30, 30);
	private static Color bnColor = new Color(150, 40, 40);
	private static Color aTextColor = Color.BLACK;
	private static Color bTextColor = Color.WHITE;
	private static Color bgColor = new Color(80, 80, 80);
	
	private static String OS = System.getProperty("os.name").toLowerCase();
	
	public static void main(String[] args) {
		System.setProperty("sun.java2d.opengl", "true");
		
		initSaves();
		
		UIManager.put("SplitPane.background", bgColor);
		
		URL iconURL = FTLSaver.class.getResource("/icon.png");
		ImageIcon icon = new ImageIcon(iconURL);
		
		frame = new JFrame(windowTitle);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setIconImage(icon.getImage());
		frame.setMinimumSize(new Dimension(150, 200));
		
		JPanel savePanel = newPanel("Backup");
		JPanel restorePanel = newPanel("Restore");
		
		saveNext = newAButton("Next");
		restoreLast = newAButton("Last");
		
	    saveNext.addActionListener(e -> backup(saves.indexOf(nextSave)));
	    restoreLast.addActionListener(e -> restore(saves.indexOf(lastSave)));
	    
		savePanel.add(saveNext);
		restorePanel.add(restoreLast);
		
		initButtons(savePanel, restorePanel);
		
		JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, savePanel, restorePanel);
		
		frame.addComponentListener(new ComponentAdapter(){
		    @Override
		    public void componentResized(ComponentEvent e) {
	            split.setDividerLocation(0.5);
		    }
		});
		frame.add(split);
		frame.pack();
		frame.setSize(new Dimension(300, 400));
		frame.setVisible(true);
		
		split.setContinuousLayout(true);
		split.setResizeWeight(0.5);
		split.setEnabled(false);
		split.setDividerSize(0);		
		
		checkAndCreateFolders();
		findLastModified();
	}
	
	private static void backup(int slot) throws RuntimeException {
		try {
			saves.get(slot).backup();
			findLastModified();
		} catch(RuntimeException r) {
			throw r;
		} catch(Exception e) {
			throw new RuntimeException("This slot is not initialized!");
		}
	}
	
	private static void restore(int slot) throws RuntimeException {
		try {
			saves.get(slot).restore();
		} catch(RuntimeException r) {
			throw r;
		} catch(Exception e) {
			throw new RuntimeException("This slot is not initialized!");
		}
	}
	
	private static void initSaves() {
		savepath = System.getProperty("user.home");
		if(OS.indexOf("win") >= 0) {
			savepath += "\\Documents\\My Games\\FasterThanLight\\";
		} else if(OS.indexOf("mac") >= 0) {
			savepath += "/Library/Application Support/FasterThanLight/";
		} else if(OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS.indexOf("aix") > 0) {
			savepath += "/.local/share/FasterThanLight/";
		} else {
			throwPopup("Error", "Could not determine system!");
			throw new RuntimeException("Could not determine system!");
		}
		
		saves = new ArrayList<Save>(5);
		for(int i = 0; i < savecount; ++i) {
			String slotpath = getSavepath(i);
			saves.add(new Save(savepath, slotpath));
		}
		lastSave = saves.get(savecount - 1);
		nextSave = saves.get(0);
	}
	
	private static JPanel newPanel(String title) {
		JPanel newPanel = new JPanel();
		newPanel.setBorder(new TitledBorder(title));
		((TitledBorder)newPanel.getBorder()).setTitleColor(Color.WHITE);
		
		newPanel.setOpaque(true);
		newPanel.setBackground(bgColor);
		
		GridLayout saveLayout = new GridLayout(savecount+1, 1);
		saveLayout.setVgap(5);
		newPanel.setLayout(saveLayout);
		
		return newPanel;
	}
	
	private static JButton newAButton(String label) {
		return new JButton(label) {
			private static final long serialVersionUID = 1L;
			{
	            setSize(300, 150);
	            setMaximumSize(getSize());
	            setForeground(aTextColor);
	            setBackground(aColor);
	            setOpaque(true);
	            setBorderPainted(false);
	        }
	    };
	}

	private static String getSavepath(int slot) {
		String path = savepath + String.format("%02d", slot);
		return path;
	}
	
	private static boolean checkAndCreateFolders() throws RuntimeException {
		for(Save save : saves) {
			try {
				save.checkFolderValidity();
			} catch(IllegalArgumentException e) {
				throwPopup("Error", "Save folder invalid!");
				throw new RuntimeException("Save folder invalid!");
			} catch(RuntimeException e) {
				throwPopup("Error", e.getMessage());
				throw e;
			}
		}
		return true;
	}
	
	private static void findLastModified() {
		long latest = 0;
		lastSave = saves.get(0);
		nextSave = saves.get(1);
		for(Save save : saves) {
			restoreButton[saves.indexOf(save)].setBackground(bColor);
			if(save.getTimestampLong() > latest) {
				latest = save.getTimestampLong();
				lastSave = save;
				nextSave = saves.get((saves.indexOf(save) + 1) % savecount);
			}
		}
		restoreButton[saves.indexOf(lastSave)].setBackground(bnColor);
	}
	
	private static void throwPopup(String title, String message) {
		String popup = title + ": " + message;
		JOptionPane.showMessageDialog(frame, popup);
	}
	
	private static void initButtons(JPanel savePanel, JPanel restorePanel) {
		saveButton = new JButton[savecount];
		restoreButton = new JButton[savecount];
		for(int i = 0; i < savecount; ++i) {
			saveButton[i] = new JButton("Slot " + i) {
				private static final long serialVersionUID = 1L;
				{
		            setSize(300, 150);
		            setMaximumSize(getSize());
		            setForeground(bTextColor);
		            setBackground(bColor);
		            setOpaque(true);
		            setBorderPainted(false);
		        }
		    };
		    final int slot = i;
		    saveButton[i].addActionListener(e -> { try { backup(slot); } catch(RuntimeException r) { throwPopup("Error", r.getMessage()); } });
		    savePanel.add(saveButton[i]);
		    
		    restoreButton[i] = new JButton("Slot " + i) {
				private static final long serialVersionUID = 1L;
				{
		            setSize(300, 150);
		            setMaximumSize(getSize());
		            setForeground(bTextColor);
		            setBackground(bColor);
		            setOpaque(true);
		            setBorderPainted(false);
		        }
		    };
		    restoreButton[i].addActionListener(e -> { try { restore(slot); } catch(RuntimeException r) { throwPopup("Error", r.getMessage()); } });
		    restorePanel.add(restoreButton[i]);
		}
		resetButtonTexts();
	}
	
	public static void setRestoreText(String text) {
		restoreLast.setText(text);
	}
	
	public static void resetButtonTexts() {
		int i = 0;
		for(JButton button : restoreButton) {
			if(!saves.get(i).isEmpty()) {
				button.setText("<html><center>Slot " + i + "<br />" + saves.get(i).getTimestamp());
			} else {
				button.setText("<html><center>Slot " + i + "<br />" + "(unsaved)");
			}
			++i;
		}
	}
	
	public static ArrayList<Save> getSaves() {
		return saves;
	}
}
