package com.backmeup.provider;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.permission.FsPermission;
import org.apache.hadoop.hdfs.DFSClient;

public class HDFSStorage extends AbstractStorageProvider {
    Configuration conf;
    DFSClient client;


    @Override
    protected boolean doUploadDirectory(String path) {
        try {
            return client.mkdirs(path, FsPermission.getDirDefault(),true);
        } catch (IOException e) {
            logger.warn("Error when create dir.");
        }
        return false;
    }

    @Override
    protected boolean doUploadFile(String source, String target) {
        BufferedOutputStream out;
        BufferedInputStream in;
        try {
            if (client.exists(target)) {
                System.out.println("File already exists in hdfs: " + target);
                return true;
            }
            out = new BufferedOutputStream(client.create(target, false));
            in = new BufferedInputStream(new FileInputStream(source));
            return copyFile(in, out);
        } catch (IOException e) {
            logger.error("Can't upload file.", e);
        }
        return false;
    }

    @Override
    protected boolean doExistTarget(String path, boolean isFile) {
        try {
            return client.exists(path);
        } catch (IOException e) {
            logger.warn("Error when detect target");
            return false;
        }
    }

    public void init() {
        conf = new Configuration();
        String hdfsUrl = (String) getProperty("Server");
        conf.set("fs.defaultFS", hdfsUrl);
        try {
            client = new DFSClient(new URI(hdfsUrl), conf);
        } catch (IOException e) {
            throw new RuntimeException("Can't init HDFS storage.", e);
        } catch (URISyntaxException e) {
            throw new RuntimeException("Can't init HDFS storage.", e);
        }

    }
}
