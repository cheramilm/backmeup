package com.backmeup;

import com.backmeup.provider.Property;
import com.backmeup.task.Task;
import com.backmeup.task.TaskManager;
import com.backmeup.task.TaskType;
import org.apache.log4j.Logger;

import java.io.File;

public class Activity extends PropertyBasedObject {
    public static final String ACTIVITY_FILE_PREFIX = "backmeup";
    public static final String ACTIVITY_FILE_EXT = ".properties";
    static Logger logger = Logger.getLogger(Activity.class);
    private String recordId;
    private String sourceFolder;
    private String targetFolder;
    private boolean forceSync;
    private RecordManager recordManager;
    private TaskManager taskManager;
    private StorageProvider storageProvider;
    private String targetRootFolder;

    private File recordFile;

    private String generateRecordId() {
        StringBuffer buffer = new StringBuffer();
        for (Property property : getProvider().getIdProperties()) {
            buffer.append(property.getName()).append(":").append(getProperty(property.getName())).append("#");
        }
        return buffer.toString();
    }

    private String generateIdString() {
        StringBuffer buffer = new StringBuffer();
        for (Property property : getProvider().getIdProperties()) {
            buffer.append(getProperty(property.getName())).append(":");
        }
        return buffer.toString().substring(0, buffer.length() - 1);
    }

    public String getRecordId() {
        return recordId;
    }

    public void init() {
        forceSync = (Boolean) getProperty("ForceSync");
        sourceFolder = (String) getProperty("SourceFolder");
        targetFolder = (String) getProperty("TargetFolder");
        recordId = generateRecordId();
        recordManager = RecordManager.getRecordManager(sourceFolder);
        storageProvider = getProvider().getStorageProvider();
        taskManager = new TaskManager((Integer) getProperty("MaxTasks"));
        applyProperties((PropertyBasedObject) storageProvider);
        storageProvider.init();
    }

    public void start() {
        analysis();
        taskManager.start();
    }

    public void waitComplete() {
        while (!taskManager.isAllTasksCompleted()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                logger.warn("Interrupted.", e);
            }
        }
    }

    public String getSourcePath(String relativePath) {
        if (relativePath.isEmpty()) {
            return new File(sourceFolder).getAbsolutePath();
        } else {
            File file = new File(sourceFolder, relativePath);
            return file.getAbsolutePath();
        }
    }

    public String getTargetPath(String relativePath) {
        if (relativePath.isEmpty()) {
            return targetRootFolder;
        } else {
            return Utils.getTargetPath(targetRootFolder, relativePath);
        }
    }


    public void analysis() {
        new Thread(new Runnable() {
            public void run() {
                File folder = new File(sourceFolder);
                if (targetFolder.equals("/")) {
                    targetRootFolder = targetFolder + folder.getName();
                } else {
                    targetRootFolder = targetFolder + "/" + folder.getName();
                }
                processRootFolder(folder);
            }
        }).start();
    }

    public void updateLocalRecord(File file) {
        recordManager.update(recordId, file);
    }

    private void processRootFolder(File rootFolder) {
        if (forceSync || !recordManager.recordExists(recordId)) {
            Task task = new Task(this, TaskType.UploadDirectory, "");
            task.setPriority(5);
            taskManager.addTask(task);
            processFolder(sourceFolder, rootFolder, task);
        } else {
            logger.info("Don't need process root directory [" + targetRootFolder + "] based on local record.");
            processFolder(sourceFolder, rootFolder, null);
        }
    }

    private void processFolder(String sourceFolder, File folder, Task parentTask) {
        Task task;
        String relativeFilePath = Utils.getRelativeFilePath(sourceFolder, folder);
        if (relativeFilePath.length() > 0) {
            if (forceSync || recordManager.needSync(recordId, relativeFilePath)) {
                task = new Task(this, TaskType.UploadDirectory, relativeFilePath);
                task.setPriority(5);
                if (parentTask != null) {
                    parentTask.addChildTask(task);
                }
                logger.debug("Add new task for " + folder.getAbsolutePath());
                taskManager.addTask(task);
            } else {
                task = parentTask;
                logger.info("Don't need process directory [" + folder + "] based on local record.");
            }
        } else {
            task = parentTask;
        }
        File[] list = folder.listFiles();
        if (list == null) return;
        for (File f : list) {
            if (f.isDirectory()) {
                processFolder(sourceFolder, f, task);
            } else {
                processFile(sourceFolder, f, task);
            }
        }
    }

    private void processFile(String sourceFolder, File file, Task parentTask) {
        if (file.getName().equals(RecordManager.FILE_NAME)) {
            logger.debug("Don't upload local record file.");
            return;
        }
        String relativeFilePath = Utils.getRelativeFilePath(sourceFolder, file);
        if (forceSync || recordManager.needSync(recordId, relativeFilePath)) {
            Task task = new Task(this, TaskType.UploadFile, relativeFilePath);
            if (parentTask != null) {
                parentTask.addChildTask(task);
            }
            logger.debug("Add new task for " + file.getAbsolutePath());
            taskManager.addTask(task);
        } else {
            logger.info("Don't need process file [" + file + "] based on local record.");
        }
    }

    public String getSourceFolder() {
        return sourceFolder;
    }

    public StorageProvider getStorageProvider() {
        return storageProvider;
    }

    public TaskManager getTaskManager() {
        return taskManager;
    }

    public String toString() {
        return sourceFolder + "->" + generateIdString();
    }

    public File getNextAvailableFile() {
        int i = 0;
        for (; i < Integer.MAX_VALUE; i++) {
            File f = new File(ACTIVITY_FILE_PREFIX + i + ACTIVITY_FILE_EXT);
            if (!f.exists()) {
                setRecordFile(f);
                return f;
            }
        }
        return null;
    }

    public File getRecordFile() {
        return recordFile;
    }

    public void setRecordFile(File recordFile) {
        this.recordFile = recordFile;
    }

    public void deleteRecordFile() {
        if (recordFile != null) {
            recordFile.delete();
        }
    }

}
