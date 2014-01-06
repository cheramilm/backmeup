package com.backmeup.provider;

import com.backmeup.Utils;

import java.io.*;

public class DiskStorage extends AbstractStorageProvider {
    @Override
    public boolean doUploadDirectory(String path) {
        File file = new File(path);
        return file.mkdirs() || file.exists();
    }

    @Override
    protected boolean doUploadFile(String source, String target) {
        try {
            BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(source));
            BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(target));
            Utils.sleep(500);
            return copyFile(inputStream, outputStream);
        } catch (FileNotFoundException e) {
            logger.error("Can't upload file.", e);
        }

        return false;
    }

    @Override
    protected boolean doExistTarget(String path, boolean isFile) {
        return new File(path).exists();
    }

    public void init() {

    }

}
