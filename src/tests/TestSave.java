package tests;
import ftlapplication.Save;

//import static org.junit.jupiter.api.Assertions.*;

import java.io.File;

import org.junit.BeforeClass;
import org.junit.AfterClass;
import org.junit.jupiter.api.Test;

class TestSave {

	String savepath = "c:\\Users\\alpay\\Documents\\My Games\\FasterThanLight\\";
	String slotpath = "c:\\Users\\alpay\\Documents\\My Games\\FasterThanLight\\test";
	
	@BeforeClass
	void SetUpClass() {
		
	}
	
	@Test
	void test() {
		Save save = new Save(savepath, slotpath);
		save.backup();
	}
	
	@AfterClass
	void TearDownClass() {
		File dir = new File(slotpath);
		String[] files = dir.list();
		for(String file : files) {
			File thisfile = new File(file);
			thisfile.delete();
		}
		dir.delete();
	}
}
