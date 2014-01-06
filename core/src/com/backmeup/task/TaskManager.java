package com.backmeup.task;

import org.apache.log4j.Logger;


import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TaskManager {
    static Logger logger = Logger.getLogger(TaskManager.class);
    private ExecutorService executorService = Executors.newCachedThreadPool();
    private Collection<Task> completedTasks;
    private Collection<Task> failedTasks = new CopyOnWriteArrayList<Task>();
    private Collection<Task> pendingTasks = new CopyOnWriteArrayList<Task>();
    private Set<Task> executableTasks = new ConcurrentSkipListSet<Task>(TaskComparator.instance);
    private Collection<Task> executingTasks = new CopyOnWriteArrayList<Task>();
    private boolean running;
    private Collection<TaskListener> taskListeners = new CopyOnWriteArrayList<TaskListener>();
    private Collection<ProgressListener> progressListeners = new CopyOnWriteArrayList<ProgressListener>();
    private int maxTasks;
    private int maxRetry=3;

    private ThreadLocal<Task> currentTask = new ThreadLocal<Task>();

    public TaskManager(int maxTasks) {
        this.maxTasks = maxTasks;
        executorService = Executors.newFixedThreadPool(maxTasks);
        completedTasks = new CopyOnWriteArrayList<Task>();
    }

    public void registerTaskListener(TaskListener listener) {
        taskListeners.add(listener);
    }


    public void removeAllTaskListeners() {
        taskListeners.clear();
    }

    public void start() {
        running = true;
        executeNextTasks();
    }

    public void stop() {
        running = false;
    }

    public synchronized void executeNextTasks() {
        while (running && executableTasks.size() > 0 && executingTasks.size() < maxTasks) {
            Iterator<Task> tasks = executableTasks.iterator();
            if (tasks.hasNext()) {
                Task next = tasks.next();
                tasks.remove();
                notifyTaskListeners(next, TaskEventType.ExecutableTaskRemoved);
                executingTasks.add(next);
                notifyTaskListeners(next, TaskEventType.ExecutingTaskAdded);
                executorService.execute(next);
            }
        }
    }

    public void addTask(Task task) {
        notifyTaskListeners(task,TaskEventType.NewTask);
        if (task.getParentTask() == null || task.getParentTask().isCompleted()) {
            executableTasks.add(task);
            notifyTaskListeners(task, TaskEventType.ExecutableTaskAdded);
            executeNextTasks();
        } else if (task.getParentTask().isFailed()) {
            failedTasks.add(task);
            notifyTaskListeners(task, TaskEventType.FailedTaskAdded);
        } else {
            pendingTasks.add(task);
            notifyTaskListeners(task, TaskEventType.PendingTaskAdded);
        }
    }

    public boolean isAllTasksCompleted() {
        return pendingTasks.size() == 0 && executableTasks.size() == 0 && executingTasks.size() == 0;
    }

    public void addCompletedTask(Task task) {
        executingTasks.remove(task);
        notifyTaskListeners(task, TaskEventType.ExecutingTaskRemoved);
        completedTasks.add(task);
        notifyTaskListeners(task, TaskEventType.CompletedTaskAdded);
        for (Task t : task.getChildTasks()) {
            pendingTasks.remove(t);
            notifyTaskListeners(task, TaskEventType.PendingTaskRemoved, TaskEventType.ExecutableTaskAdded);
            executableTasks.add(t);
            notifyTaskListeners(task, TaskEventType.ExecutableTaskAdded, TaskEventType.PendingTaskRemoved);
        }
        executeNextTasks();
    }

    private void notifyTaskListeners(Task task, TaskEventType type) {
        notifyTaskListeners(task, type, null);
    }

    public void notifyTaskListeners(Task task, TaskEventType type, TaskEventType relatedEventType) {
        TaskEvent event = new TaskEvent(task, type, relatedEventType);
        for (TaskListener listener : taskListeners) {
            listener.taskChanged(event);
        }
    }

    public void addRetryTask(Task task) {
        executingTasks.remove(task);
        notifyTaskListeners(task, TaskEventType.ExecutingTaskRemoved);
        executableTasks.add(task);
        notifyTaskListeners(task, TaskEventType.ExecutableTaskAdded);
        executeNextTasks();
    }

    public void addFailedTask(Task task) {
        executingTasks.remove(task);
        notifyTaskListeners(task, TaskEventType.ExecutingTaskRemoved);
        failedTasks.add(task);
        notifyTaskListeners(task, TaskEventType.FailedTaskAdded);
        for (Task t : task.getChildTasks()) {
            addFailedTask(t);
        }
        executeNextTasks();
    }

    public void startProcess(Task task) {
        currentTask.set(task);
        Progress progress = new Progress();
        task.setProgress(progress);
        progress.setStartTime(System.currentTimeMillis());
        progress.setTotalLength(task.getTaskInfo().getSize());
        logger.debug("Start task:" + task);
    }

    public void processed(long bytes) {
        Task task = currentTask.get();
        Progress progress = task.getProgress();
        progress.addCompletedLength(bytes);
        for (ProgressListener listener : progressListeners) {
            listener.progressChanged(task, progress);
        }
    }

    public void completed() {
        Task task = currentTask.get();
        Progress progress = task.getProgress();
        progress.setEndTime(System.currentTimeMillis());
        currentTask.remove();
    }

    public void registerProgressListener(ProgressListener listener) {
        progressListeners.add(listener);
    }

    public void removeAllProgressListeners() {
        progressListeners.clear();
    }

    public void clearTasks() {
        stop();
        clearTasks(executingTasks,TaskEventType.ExecutingTaskRemoved);
        clearTasks(pendingTasks, TaskEventType.PendingTaskRemoved);
        clearTasks(executableTasks, TaskEventType.ExecutableTaskRemoved);
        clearTasks(failedTasks, TaskEventType.FailedTaskRemoved);
        removeAllProgressListeners();
        removeAllTaskListeners();
    }

    private void clearTasks(Collection<Task> tasks, TaskEventType type) {
        for (Task task : tasks) {
            tasks.remove(task);
            notifyTaskListeners(task, type);
        }
    }

    public int getMaxRetry() {
        return maxRetry;
    }

    public void setMaxRetry(int maxRetry) {
        this.maxRetry = maxRetry;
    }
}
