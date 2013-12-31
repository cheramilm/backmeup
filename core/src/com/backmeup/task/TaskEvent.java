package com.backmeup.task;

public class TaskEvent {
    private Task source;
    private TaskEventType type;
    private TaskEventType relatedEventType;

    public TaskEvent(Task source, TaskEventType type) {
        this.source = source;
        this.type = type;
    }

    public TaskEvent(Task source, TaskEventType type, TaskEventType relatedEventType) {
        this.source = source;
        this.type = type;
        this.relatedEventType = relatedEventType;
    }


    public Task getSource() {
        return source;
    }

    public TaskEventType getType() {
        return type;
    }

    public TaskEventType getRelatedEventType() {
        return relatedEventType;
    }
}
