package com.backmeup.provider;

import com.backmeup.PropertyBasedObject;
import com.backmeup.StorageProvider;
import com.backmeup.task.Task;
import com.backmeup.task.TaskManager;
import org.apache.log4j.Logger;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;

public abstract class AbstractStorageProvider extends PropertyBasedObject implements StorageProvider {
    static Logger logger = Logger.getLogger(AbstractStorageProvider.class);
    private ThreadLocal<TaskManager> currentTaskManager = new ThreadLocal<TaskManager>();

    public TaskManager getCurrentTaskManager() {
        return currentTaskManager.get();
    }

    public boolean execute(Task task) {
        switch (task.getType()) {
            case UploadFile:
                TaskManager taskManager=task.getActivity().getTaskManager();
                currentTaskManager.set(taskManager);
                taskManager.startProcess(task);
                logger.debug("Start store file:" + task.getFile());
                if (uploadFile(task.getSourcePath(), task.getTargetPath())) {
                    taskManager.completed();
                    return true;
                } else {
                    return false;
                }
            case UploadDirectory:
                return uploadDirectory(task.getTargetPath());
        }
        return false;
    }

    protected boolean virtualDirectory() {
        return false;
    }

    public boolean uploadDirectory(String path) {
        if (!virtualDirectory()) {
            return doUploadDirectory(path);
        } else {
            logger.debug("Virtual directory, don't need create folder.");
            return true;
        }
    }

    protected abstract boolean doUploadDirectory(String path);

    public boolean uploadFile(String source, String target) {
        return doUploadFile(source, target);
    }

    protected abstract boolean doUploadFile(String source, String target);

    public boolean existTarget(String target, boolean isFile) {
        return doExistTarget(target, isFile);
    }

    protected abstract boolean doExistTarget(String path, boolean isFile);

    public void close() {
    }

    protected boolean copyFile(BufferedInputStream inputStream, BufferedOutputStream outputStream) {
        return copyFile(inputStream, outputStream, (Integer)getProperty("BufferSize"), true);
    }

    protected boolean copyFile(BufferedInputStream inputStream, BufferedOutputStream outputStream, int bufferSize, boolean closeStreamsAfterCopy) {
        try {
            byte[] buffers = new byte[bufferSize];
            int length;
            while ((length = inputStream.read(buffers)) != -1) {
                outputStream.write(buffers, 0, length);
                currentTaskManager.get().processed(length);
            }
            return true;
        } catch (IOException e) {
            logger.warn("Can't copy file.", e);
            return false;
        } finally {
            if (closeStreamsAfterCopy) {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        logger.warn("Can't close input stream.", e);
                    }
                }
                if (outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (IOException e) {
                        logger.warn("Can't close output stream.", e);
                    }
                }
            }
        }
    }

}
