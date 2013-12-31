package com.backmeup;

import com.backmeup.task.Task;

public interface StorageProvider {
    public void init();
    public boolean execute(Task task);
    public void close();
}
