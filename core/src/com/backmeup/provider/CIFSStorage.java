package com.backmeup.provider;

import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileOutputStream;
import org.apache.log4j.Logger;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.UnknownHostException;

public class CIFSStorage extends AbstractStorageProvider {
    static Logger logger = Logger.getLogger(CIFSStorage.class);
    private String domain;
    private String username;
    private String password;
    private String server;
    private NtlmPasswordAuthentication auth;
    private String rootURL;

    private String getPath(String path) {
        return rootURL + path;
    }

    @Override
    public boolean doUploadDirectory(String path) {
        SmbFile sFile;
        try {
            logger.debug("Start create SmbFile:" + path);
            sFile = new SmbFile(getPath(path), auth);
            if (!sFile.exists()) {
                sFile.mkdirs();
            } else {
                logger.debug("Target file exists, don't need create, ignored.");
            }
            return true;
        } catch (MalformedURLException e) {
            logger.warn("Incorrect path:" + path, e);
        } catch (SmbException e) {
            logger.warn("Can't create directory.", e);
        }
        return false;
    }

    @Override
    protected boolean doUploadFile(String source, String target) {
        try {
            BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(source));
            SmbFile sFile = new SmbFile(getPath(target), auth);
            BufferedOutputStream outputStream = new BufferedOutputStream(new SmbFileOutputStream(sFile));
            return copyFile(inputStream, outputStream);
        } catch (MalformedURLException e) {
            logger.warn("Invalid path:" + target, e);
        } catch (UnknownHostException e) {
            logger.warn("Unknown server:" + target, e);
        } catch (SmbException e) {
            logger.warn("SMB exception.", e);
        } catch (IOException e) {
            logger.warn("IO Exception.", e);
        }
        return false;
    }

    @Override
    protected boolean doExistTarget(String path, boolean isFile) {
        SmbFile sFile;
        try {
            sFile = new SmbFile(getPath(path), auth);
            return sFile.exists();
        } catch (MalformedURLException e) {
            logger.warn("Wrong path?" + path, e);
        } catch (SmbException e) {
            logger.warn("SMB exception when determine path:" + path, e);
        }
        return false;
    }

    public void init() {
        jcifs.Config.setProperty("jcifs.resolveOrder", (String)getProperty("ResolveOrder"));
        domain=(String)getProperty("Domain");
        username = (String)getProperty("UserName");
        password = (String)getProperty("Password");
        server = (String)getProperty("Server");
        if (domain!=null) {
            auth = new NtlmPasswordAuthentication(domain, username, password);
        } else {
            auth = new NtlmPasswordAuthentication(username + ":" + password);
        }
        rootURL = (String)getProperty("URLPrefix") + server;
    }
}
