package com.backmeup.ui;

import com.backmeup.task.Progress;
import com.backmeup.task.ProgressListener;
import com.backmeup.task.*;
import org.apache.log4j.Logger;

import javax.swing.table.AbstractTableModel;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TaskTableModel extends AbstractTableModel implements TaskListener, ProgressListener {
    static Logger logger = Logger.getLogger(TaskTableModel.class);
    private static String[] columnNames = {UIContext.getResource("taskPane.detailHeader.file"),
            UIContext.getResource("taskPane.detailHeader.size"),
            UIContext.getResource("taskPane.detailHeader.priority"),
            UIContext.getResource("taskPane.detailHeader.storageProvider"),
            UIContext.getResource("taskPane.detailHeader.targetFile"),
            UIContext.getResource("taskPane.detailHeader.status")};

    private TaskEventType[] eventTypes;
    private List<Task> tasks;
    private boolean executing;
    private static final long serialVersionUID = -6590072282169559018L;

    public TaskTableModel(TaskEventType[] eventTypes,boolean useSortedList) {
        this.eventTypes = eventTypes;
        executing = isExecutingTable();
        if (useSortedList) {
            tasks = new SortedList<Task>();
        } else {
            tasks = Collections.synchronizedList(new ArrayList<Task>());
        }
    }

    public boolean isExecutingTable() {
        for (TaskEventType type : eventTypes) {
            if (type == TaskEventType.ExecutingTaskAdded || type == TaskEventType.ExecutingTaskRemoved) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }

    public int getRowCount() {
        return tasks.size();
    }

    public int getColumnCount() {
        return columnNames.length;
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        if (tasks.size() <= rowIndex) {
            return null;
        }
        Task task = tasks.get(rowIndex);
        switch (columnIndex) {
            case 0:
                return task.getFile().getAbsolutePath();
            case 1:
                return Utils.getFileSize(task.getTaskInfo().getSize());
            case 2:
                return task.getPriority();
            case 3:
                return task.getActivity().getProvider().getName();
            case 4:
                return task.getActivity().getTargetPath(task.getTarget());
            case 5:
                if (executing) {
                    Progress progress = task.getProgress();
                    if (progress != null) {
                        return progress.getCompletedPercentage()+","+progress.getCurrentSpeed();
                    } else {
                        return 0;
                    }

                } else {
                    return task.getStatus();
                }
        }
        return task;
    }


    public synchronized void taskChanged(TaskEvent event) {
        TaskEventType type = event.getType();
        TaskEventType relatedType = event.getRelatedEventType();
        Task task = event.getSource();

        if (eventTypes != null && eventTypes.length > 0) {
            for (TaskEventType t : eventTypes) {
                if (type == t) {
                    //logger.error("Task:"+task.getFile()+", event:"+type);
                    if (type == TaskEventType.PendingTaskRemoved && relatedType == TaskEventType.ExecutableTaskAdded) {
                        return;
                    }
                    if (type == TaskEventType.ExecutableTaskAdded && relatedType == TaskEventType.PendingTaskRemoved) {
                        return;
                    }
                    if (type.toString().endsWith("Added")) {
                        tasks.add(task);
                        int index = tasks.indexOf(task);
                        this.fireTableRowsUpdated(index, index + 1);
                    } else {
                        int index = tasks.indexOf(task);
                        if (index == -1) {
                            logger.warn("Can't find task:" + task);
                        } else {
                            tasks.remove(index);
                            this.fireTableRowsDeleted(index, index + 1);
                        }
                    }
                }
            }
        }
    }

    public void progressChanged(Task task, Progress progress) {
        int index = tasks.indexOf(task);
        if (index >= 0) {
            this.fireTableCellUpdated(index, 5);
        }
    }

    public class SortedList<E> extends AbstractList<E> {

        private List<E> internalList = Collections.synchronizedList(new ArrayList<E>());

        @Override
        public void add(int position, E e) {
            internalList.add(e);
            Collections.sort(internalList, null);
        }

        @Override
        public E get(int i) {
            return internalList.get(i);
        }

        @Override
        public int size() {
            return internalList.size();
        }

        @Override
        public E remove(int index) {
            return internalList.remove(index);
        }
    }
}
