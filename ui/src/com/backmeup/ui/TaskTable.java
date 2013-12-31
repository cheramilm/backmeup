package com.backmeup.ui;

import com.backmeup.task.ProgressListener;
import com.backmeup.task.TaskEvent;
import com.backmeup.task.TaskEventType;
import com.backmeup.task.TaskListener;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;

public class TaskTable extends JTable implements TaskListener {
    private boolean executing;
    ProgressCellRender progressBar = new ProgressCellRender();
    private TaskTableModel tableModel;

    public void taskChanged(TaskEvent event) {

    }

    public TaskTable(TaskEventType[] eventTypes,boolean useSortedList) {
        super(new TaskTableModel(eventTypes,useSortedList));
        tableModel=(TaskTableModel)this.getModel();
        setColumnWidth(1, 60);
        setColumnWidth(2, 60);
        setColumnWidth(3, 80);
        setColumnWidth(5, 200);
        setFillsViewportHeight(true);
        executing=tableModel.isExecutingTable();
    }

    public boolean isExecutingTable() {
        return executing;
    }

    public TaskListener getTaskListener() {
        return tableModel;
    }


    public ProgressListener getProgressListener() {
        if (executing) {
            return tableModel;
        } else {
            return null;
        }
    }

    private void setColumnWidth(int column, int width) {
        getColumnModel().getColumn(column).setPreferredWidth(width);
        getColumnModel().getColumn(column).setMinWidth(width);
        getColumnModel().getColumn(column).setMaxWidth(width);
    }

    public TableCellRenderer getCellRenderer(int row, int column) {
        switch (column) {
            case 5:
                if (executing) {
                    return progressBar;
                }

        }
        return super.getCellRenderer(row, column);
    }

}
