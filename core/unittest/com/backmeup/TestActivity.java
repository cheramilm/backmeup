package com.backmeup;

import org.junit.Test;

public class TestActivity {
    private void testFromFile(String path) throws ApplicationException {
        Activity activity = new Activity();
        activity.loadFromResource(path);
        activity.init();
        activity.start();
        activity.waitComplete();
    }

    @Test
    public void testFtpActivity() throws ApplicationException {
        testFromFile("/sync_ftp.properties");
    }

    @Test
    public void testCIFSActivity() throws ApplicationException {
        testFromFile("/sync_cifs.properties");
    }

    @Test
    public void testFolderActivity() throws ApplicationException {
        testFromFile("/sync_folder.properties");
    }

    @Test
    public void testSaveActivity() throws ApplicationException {
        Activity activity = new Activity();
        activity.loadFromResource("/save_chinese.properties");
        activity.save("save_chinese.properties");
    }
}
