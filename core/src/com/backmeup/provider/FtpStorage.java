package com.backmeup.provider;

import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.*;
import org.apache.commons.net.io.CopyStreamEvent;
import org.apache.commons.net.io.CopyStreamListener;
import org.apache.commons.net.util.TrustManagerUtils;
import org.apache.log4j.Logger;

import java.io.*;

public class FtpStorage extends AbstractStorageProvider {
    static Logger logger = Logger.getLogger(FtpStorage.class);
    private static String LOCAL_CHARSET = "GBK";
    private static final String SERVER_CHARSET = "ISO-8859-1";
    private ThreadLocal<FTPClient> ftpClientThreadLocal = new ThreadLocal<FTPClient>();
    boolean binaryTransfer, error = false, listHiddenFiles;
    boolean localActive, useEpsvWithIPv4;
    long keepAliveTimeout;
    int controlKeepAliveReplyTimeout;
    int bufferSize;
    boolean protocol;
    String trustmgr;
    String proxyHost;
    int proxyPort;
    String proxyUser;
    String proxyPassword;
    String username;
    String password;

    String server;
    int port;

    boolean supportContinueUpload;

    public FtpStorage() {
    }

    private void abandonCurrentConnection() {
        ftpClientThreadLocal.set(null);
    }

    private String getFtpString(String value)
            throws UnsupportedEncodingException {
        return new String(value.getBytes(LOCAL_CHARSET), SERVER_CHARSET);
    }


    public void init() {
        binaryTransfer = (Boolean) getProperty("BinaryTransfer");
        listHiddenFiles = (Boolean) getProperty("ListHiddenFiles");
        localActive = (Boolean) getProperty("ActiveMode");
        useEpsvWithIPv4 = (Boolean) getProperty("EPSVwithIPv4");
        keepAliveTimeout = (Long) getProperty("KeepAliveTimeout");
        controlKeepAliveReplyTimeout = (Integer) getProperty("ControlKeepAliveReplyTimeout");

        bufferSize = (Integer) getProperty("BufferSize");
        protocol = (Boolean) getProperty("UseSSL"); // SSL protocol
        trustmgr = (String) getProperty("TrustManager");
        proxyHost = (String) getProperty("ProxyServer");
        proxyPort = (Integer) getProperty("ProxyPort");
        proxyUser = (String) getProperty("ProxyUserName");
        proxyPassword = (String) getProperty("ProxyPassword");
        username = (String) getProperty("UserName");
        password = (String) getProperty("Password");

        server = (String) getProperty("Server");
        port = (Integer) getProperty("Port");
    }

    private FTPClient getFTPClient() {
        if (ftpClientThreadLocal.get() != null && ftpClientThreadLocal.get().isConnected()) {
            return ftpClientThreadLocal.get();
        } else {
            FTPClient ftpClient = getNewClient();
            ftpClientThreadLocal.set(ftpClient);
            ProgressMonitor monitor = new ProgressMonitor();
            ftpClient.setCopyStreamListener(monitor);
            return ftpClient;
        }
    }

    private FTPClient getNewClient() {
        FTPClient ftp;
        if (!protocol) {
            if (proxyHost != null) {
                logger.info("Using HTTP proxy server: " + proxyHost);
                ftp = new FTPHTTPClient(proxyHost, proxyPort, proxyUser, proxyPassword);
            } else {
                ftp = new FTPClient();
            }
        } else {
            FTPSClient ftps = new FTPSClient(true);
            ftp = ftps;
            if ("all".equals(trustmgr)) {
                ftps.setTrustManager(TrustManagerUtils.getAcceptAllTrustManager());
            } else if ("valid".equals(trustmgr)) {
                ftps.setTrustManager(TrustManagerUtils.getValidateServerCertificateTrustManager());
            } else if ("none".equals(trustmgr)) {
                ftps.setTrustManager(null);
            }
        }
        if (keepAliveTimeout >= 0) {
            ftp.setControlKeepAliveTimeout(keepAliveTimeout);
        }
        if (controlKeepAliveReplyTimeout >= 0) {
            ftp.setControlKeepAliveReplyTimeout(controlKeepAliveReplyTimeout);
        }
        ftp.setListHiddenFiles(listHiddenFiles);

        ftp.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(System.out), true));

        try {
            int reply;
            if (port > 0) {
                ftp.connect(server, port);
            } else {
                ftp.connect(server);
            }
            logger.info("Connected to " + server + " on " + (port > 0 ? port : ftp.getDefaultPort()));

            // After connection attempt, you should check the reply code to verify
            // success.
            reply = ftp.getReplyCode();

            if (!FTPReply.isPositiveCompletion(reply)) {
                ftp.disconnect();
                logger.error("FTP server refused connection.");
                return null;
            }
        } catch (IOException e) {
            if (ftp.isConnected()) {
                try {
                    ftp.disconnect();
                } catch (IOException f) {
                    logger.debug("Can't disconnect.", f);
                }
            }
            logger.error("Could not connect to server.", e);
            return null;
        }

        __main:
        try {
            if (!ftp.login(username, password)) {
                ftp.logout();
                error = true;
                break __main;
            }
            logger.info("Remote system is " + ftp.getSystemType());

            if (binaryTransfer) {
                ftp.setFileType(FTP.BINARY_FILE_TYPE);
            } else {
                // in theory this should not be necessary as servers should default to ASCII
                // but they don't all do so - see NET-500
                ftp.setFileType(FTP.ASCII_FILE_TYPE);
            }

            // Use passive mode as default because most of us are
            // behind firewalls these days.
            if (localActive) {
                ftp.enterLocalActiveMode();
            } else {
                ftp.enterLocalPassiveMode();
            }

            ftp.setUseEPSVwithIPv4(useEpsvWithIPv4);

            ftp.setBufferSize(bufferSize);
            if (FTPReply.isPositiveCompletion(ftp.sendCommand("OPTS UTF8", "ON"))) {
                LOCAL_CHARSET = "UTF-8";
            }
            ftp.setControlEncoding(LOCAL_CHARSET);
            if (FTPReply.isPositiveCompletion(ftp.sendCommand("REST 1"))) {
                supportContinueUpload = true;
            }
        } catch (FTPConnectionClosedException e) {
            error = true;
            logger.error("Server closed connection.", e);
        } catch (IOException e) {
            error = true;
            logger.error("Server IO error.", e);
        }
        return ftp;
    }

    public boolean doUploadFile(String source, String target) {
        InputStream input = null;
        try {
            File f = new File(source);
            input = new BufferedInputStream(new FileInputStream(f));
            FTPClient ftp = getFTPClient();
            ProgressMonitor monitor = (ProgressMonitor) ftp.getCopyStreamListener();
            if (ftp.storeFile(getFtpString(target), input)) {
                return true;
            } else {
                return false;
            }
        } catch (FileNotFoundException e) {
            logger.error("Can't find target file.", e);
            return false;
        } catch (IOException e) {
            logger.error("Can't read target file.", e);
            abandonCurrentConnection();
            return false;
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    logger.warn("Can't close target file.", e);
                }
            }
        }
    }

    @Override
    protected boolean doExistTarget(String path, boolean isFile) {
        try {
            FTPClient ftpClient = getFTPClient();
            int returnCode;
            if (!isFile) {
                ftpClient.changeWorkingDirectory(getFtpString(path));
                returnCode = ftpClient.getReplyCode();
                if (returnCode == 550) {
                    return false;
                }
                return true;
            } else {
                InputStream inputStream = ftpClient.retrieveFileStream(getFtpString(path));
                returnCode = ftpClient.getReplyCode();
                if (inputStream == null || returnCode == 550) {
                    return false;
                }
                return true;
            }
        } catch (IOException e) {
            logger.warn("Can't change working directory to " + path);
            abandonCurrentConnection();
        }
        return false;
    }

    protected boolean doUploadDirectory(String path) {
        try {
            FTPClient ftpClient = getFTPClient();
            int returnCode;
            if (ftpClient.makeDirectory(getFtpString(path))) {
                return true;
            } else {
                returnCode = ftpClient.getReplyCode();
                if (returnCode == 550) {
                    return true;
                } else {
                    return false;
                }
            }
        } catch (IOException e) {
            logger.error("Can't create directory:" + path, e);
            abandonCurrentConnection();
            return false;
        }
    }

    private class ProgressMonitor implements CopyStreamListener {
        public void bytesTransferred(CopyStreamEvent copyStreamEvent) {
            bytesTransferred(copyStreamEvent.getTotalBytesTransferred(), copyStreamEvent.getBytesTransferred(), copyStreamEvent.getStreamSize());
        }


        public void bytesTransferred(long totalBytesTransferred,
                                     int bytesTransferred, long streamSize) {
            getCurrentTaskManager().processed(bytesTransferred);
        }
    }
}
