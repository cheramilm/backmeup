package com.backmeup;

import org.apache.log4j.Logger;

import java.io.*;
import java.util.*;

public class RecordManager implements Serializable {
    private static final long serialVersionUID = -4699540388320195972L;
    static Logger logger = Logger.getLogger(RecordManager.class);
    private Map<String, FolderRecord> records = new HashMap<String, FolderRecord>();
    public static final String FILE_NAME = "backmeup.record";
    private String rootDirectory;
    private long lastSave;
    private int changeCount;

    private static Map<String, RecordManager> managers = new HashMap<String, RecordManager>();

    private RecordManager(String rootDirectory) {
        this.rootDirectory = rootDirectory;
    }

    public synchronized static RecordManager getRecordManager(String rootDirectory) {
        if (managers.containsKey(rootDirectory)) {
            return managers.get(rootDirectory);
        } else {
            RecordManager recordManager = initRecordManager(rootDirectory);
            managers.put(rootDirectory, recordManager);
            return recordManager;
        }
    }

    public void update(String recordId, File file) {
        changeCount++;
        FolderRecord folderRecord = records.get(recordId);
        if (folderRecord == null) {
            folderRecord = new FolderRecord();
            records.put(recordId, folderRecord);
        }
        String relativeFilePath = Utils.getRelativeFilePath(rootDirectory, file);
        FileRecord fileRecord = new FileRecord();
        fileRecord.setLastModifyDate(new Date(file.lastModified()));
        fileRecord.setSize(file.length());
        fileRecord.setLastRecordDate(new Date());
        folderRecord.addFileRecord(relativeFilePath, fileRecord);
        if (needChange()) {
            save();
        }
    }

    public boolean recordExists(String recordId) {
        return records.get(recordId) != null;
    }

    public boolean needSync(String recordId, String relativeFilePath) {
        FolderRecord folderRecord = records.get(recordId);
        if (folderRecord == null) {
            return true;
        } else {
            FileRecord fileRecord = folderRecord.getFileRecord(relativeFilePath);
            if (fileRecord == null) {
                return true;
            } else {
                File file = new File(rootDirectory + "/" + relativeFilePath);
                Date lastModifyDate = new Date(file.lastModified());
                return lastModifyDate.compareTo(fileRecord.getLastModifyDate()) > 0;
            }
        }
    }


    private static String getRecordFile(String rootDirectory) {
        return rootDirectory + "/" + FILE_NAME;
    }

    private boolean needChange() {
        return changeCount > 10 || (lastSave - System.currentTimeMillis() > 60000);
    }

    public synchronized void save() {
        File file = new File(getRecordFile(rootDirectory));
        try {
            ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
            oos.writeObject(this);
            oos.close();
            changeCount = 0;
            lastSave = System.currentTimeMillis();
        } catch (IOException e) {
            logger.error("Can't save local record", e);
        }
    }

    public static synchronized void saveAll() {
        for (RecordManager manager : managers.values()) {
            manager.save();
        }
    }

    private static RecordManager initRecordManager(String rootDirectory) {
        File file = new File(getRecordFile(rootDirectory));
        RecordManager recordManager = null;
        ObjectInputStream ois = null;
        try {
            if (file.exists() && file.length() > 0) {
                ois = new ObjectInputStream(new FileInputStream(file));
                recordManager = (RecordManager) ois.readObject();
                if (recordManager.records == null) {
                    recordManager.records = new HashMap<String, FolderRecord>();
                }
            } else {
                recordManager = new RecordManager(rootDirectory);
                recordManager.save();
            }
        } catch (FileNotFoundException e) {
            logger.error("Can't find target file:" + file, e);
        } catch (ClassNotFoundException e) {
            logger.error("Can't find class", e);
        } catch (IOException e) {
            logger.error("Can't read file:" + file, e);
        } finally {
            if (ois != null) {
                try {
                    ois.close();
                } catch (IOException e) {
                    logger.warn("Can't not close input file.");
                }
            }
        }
        return recordManager;
    }
}
