package com.backmeup.task;

import java.util.Comparator;

public class TaskComparator implements Comparator<Task> {
    public static TaskComparator instance=new TaskComparator();

    public static int compareTo(Task source, Task target) {
        if (source.getPriority() != target.getPriority()) {
            return source.getPriority() - target.getPriority();
        } else if(source.getTaskInfo().getSize() != target.getTaskInfo().getSize()) {
            return compareLong(source.getTaskInfo().getSize(),target.getTaskInfo().getSize());
        } else {
            return source.toString().compareTo(target.toString());
        }
    }

    public int compare(Task o1, Task o2) {
        return TaskComparator.compareTo(o1, o2);
    }



    private static int compareLong(long source,long target) {
        long result=source-target;
        if (result>=Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        } else if (result<=Integer.MIN_VALUE) {
            return Integer.MIN_VALUE;
        } else {
            return (int)result;
        }
    }


}
