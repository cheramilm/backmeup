package com.backmeup.task;

import com.backmeup.Activity;
import com.backmeup.Utils;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.Collection;
import java.util.Date;
import java.util.concurrent.CopyOnWriteArrayList;

public class Task implements Runnable, Comparable {
    static Logger logger = Logger.getLogger(Task.class);
    private Activity activity;
    private Task parentTask;
    private Collection<Task> childTasks = new CopyOnWriteArrayList<Task>();
    private TaskType type;
    private String target;
    private int priority = 10;
    private TaskStatus status = TaskStatus.Active;
    private TaskInfo taskInfo;
    private Progress progress;
    private File file;
    private int failedCount;


    public Task(Activity activity, TaskType type, String target) {
        this.activity = activity;
        this.type = type;
        this.target = target;
        taskInfo = new TaskInfo();
        file = new File(activity.getSourcePath(target));
        initTaskInfo();
    }

    public String getSourcePath() {
        return file.getAbsolutePath();
    }

    public String getTargetPath() {
        return activity.getTargetPath(target);
    }

    private void markFailed() {
        failedCount++;
        if (failedCount<activity.getTaskManager().getMaxRetry()) {
            activity.getTaskManager().addRetryTask(this);
        } else {
            status = TaskStatus.Failed;
            activity.getTaskManager().addFailedTask(this);
        }
    }

    private void markCompleted() {
        status = TaskStatus.Completed;
        activity.getTaskManager().addCompletedTask(this);
    }

    private void initTaskInfo() {
        if (file.isFile()) {
            taskInfo.setFileExtensionName(Utils.getExtensionName(file.getName()));
            taskInfo.setSize(file.length());
            taskInfo.setLastModifyTime(new Date(file.lastModified()));
            taskInfo.setDirectoryLevel(Utils.getDirectoryLevel(activity.getSourceFolder(), file));
        }
    }

    public void run() {
        try {
            taskInfo.setStartTime(new Date());
            if (activity.getStorageProvider().execute(this)) {
                taskInfo.setCompleteTime(new Date());
                taskInfo.setWorkingTime(taskInfo.getCompleteTime().getTime() - taskInfo.getStartTime().getTime());
                activity.updateLocalRecord(file);
                markCompleted();
            } else {
                markFailed();
            }
        } catch (Exception e) {
            logger.debug("Task failed.", e);
            markFailed();
        }
    }

    public Collection<Task> getChildTasks() {
        return childTasks;
    }

    public void addChildTask(Task task) {
        childTasks.add(task);
        task.parentTask = this;
    }

    public boolean isCompleted() {
        return status == TaskStatus.Completed;
    }

    public boolean isFailed() {
        return status == TaskStatus.Failed;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public Task getParentTask() {
        return parentTask;
    }

    public void setParentTask(Task parentTask) {
        this.parentTask = parentTask;
    }

    public TaskInfo getTaskInfo() {
        return taskInfo;
    }

    public int compareTo(Object o) {
        return TaskComparator.compareTo(this, (Task) o);
    }

    public File getFile() {
        return file;
    }

    public String getTarget() {
        return target;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public Progress getProgress() {
        return progress;
    }

    public void setProgress(Progress progress) {
        this.progress = progress;
    }

    public TaskType getType() {
        return type;
    }

    public Activity getActivity() {
        return activity;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    public String toString() {
        return "[" + priority + "]" + file.getAbsolutePath() + "->" + activity.getRecordId();
    }
}
