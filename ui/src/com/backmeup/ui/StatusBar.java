package com.backmeup.ui;

import com.backmeup.task.*;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;

public class StatusBar extends JLabel implements TaskListener, ProgressListener {
    long totalSize;
    long totalFile;
    long failed;
    long completed;
    Map<Task, Progress> progressMap = new HashMap<Task, Progress>();
    long lastUpdate;
    float averageSpeed;
    int speedSample;
    long firstStart;
    long totalCompletedSize;

    public StatusBar(String text) {
        super(text);
    }

    public synchronized void taskChanged(TaskEvent event) {
        TaskEventType type = event.getType();
        Task task = event.getSource();
        if (task.getType().equals(TaskType.UploadFile)) {
            if (type.equals(TaskEventType.NewTask)) {
                totalFile++;
                totalSize += task.getTaskInfo().getSize();
            } else if (type.equals(TaskEventType.CompletedTaskAdded)) {
                totalFile--;
                totalSize -= task.getTaskInfo().getSize();
                totalCompletedSize+=task.getTaskInfo().getSize();
                progressMap.remove(task);
                completed++;
            } else if (type.equals(TaskEventType.FailedTaskAdded)) {
                totalFile--;
                totalSize -= task.getTaskInfo().getSize();
                progressMap.remove(task);
                failed++;
            }
        }
        updateStatus();
    }

    public synchronized void progressChanged(Task task, Progress progress) {
        if (firstStart==0) {
            firstStart=System.currentTimeMillis();
        }
        progressMap.put(task, progress);
        updateStatus();
    }

    private void calculateAverageSpeed(float currentSpeed) {
        averageSpeed = (averageSpeed * speedSample + currentSpeed) / (speedSample + 1);
        long seconds=System.currentTimeMillis()-firstStart/1000;
        if (seconds>0) {
            float anotherAverageSpeed=totalCompletedSize/2014/seconds;
            if (anotherAverageSpeed>averageSpeed) {
                averageSpeed=anotherAverageSpeed;
            }
        }
        speedSample++;
        if (speedSample>20) {
            speedSample=1;
        }
    }

    private void updateStatus() {
        long now = System.currentTimeMillis();
        if (now - lastUpdate < 1000) {
            return;
        }
        float currentSpeed = getTotalSpeed();
        calculateAverageSpeed(currentSpeed);
        StringBuffer buffer = new StringBuffer();
        appendItem(buffer, "totalFiles", totalFile);
        appendItem(buffer, "size", Utils.getFileSize(totalSize));
        appendItem(buffer, "speed", Utils.getSpeed(currentSpeed));
        appendItem(buffer, "estimatedTime", Utils.getLeftTime(getLeftSeconds()));
        appendItem(buffer, "completed", completed);
        appendItem(buffer, "failed", failed);
        appendItem(buffer, "completedSize", Utils.getFileSize(totalCompletedSize), false);
        setText(buffer.toString());
        lastUpdate = now;
    }

    private void appendItem(StringBuffer buffer, String name, Object value) {
        appendItem(buffer, name, value, true);
    }

    private void appendItem(StringBuffer buffer, String name, Object value, boolean addComma) {
        buffer.append(UIContext.getResource(name)).append(":").append(value);
        if (addComma) buffer.append(" ");
    }

    private float getTotalSpeed() {
        float totalSpeed = 0;
        for (Progress progress : progressMap.values()) {
            totalSpeed += progress.getCurrentSpeed();
        }
        return totalSpeed;
    }

    private long getLeftTotalSize() {
        return totalSize - getTotalUploaded();
    }

    private long getLeftSeconds() {
        if (averageSpeed == 0) return Long.MAX_VALUE;
        return (long) (getLeftTotalSize() / (averageSpeed * 1024));
    }

    private long getTotalUploaded() {
        long size = 0;
        for (Progress progress : progressMap.values()) {
            size += progress.getCompletedLength();
        }
        return size;
    }
}
