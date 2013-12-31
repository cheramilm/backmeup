package com.backmeup.ui;

import com.backmeup.Activity;
import com.backmeup.ApplicationException;
import com.backmeup.task.ProgressListener;
import com.backmeup.task.TaskListener;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ActivityList extends JList<Activity> {
    static Logger logger = Logger.getLogger(ActivityList.class);
    private List<Activity> activities = new ArrayList<Activity>();
    private List<TaskListener> taskListeners = new ArrayList<TaskListener>();
    private List<ProgressListener> progressListeners = new ArrayList<ProgressListener>();
    DefaultListModel<Activity> listModel;

    public ActivityList() {
        listModel = new DefaultListModel<Activity>();
        File file = new File(".");
        File[] files = file.listFiles(new ConfigureFileFilter());
        for (File f : files) {
            Activity activity = new Activity();
            activities.add(activity);
            try {
                activity.loadFrom(f);
                activity.setRecordFile(f);
            } catch (ApplicationException e) {
                logger.warn("Can't load configure.", e);
            }
            activity.init();
            listModel.addElement(activity);
        }
        this.setModel(listModel);
        setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        setLayoutOrientation(JList.VERTICAL);
        setVisibleRowCount(-1);
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                JList list = (JList) e.getSource();
                if (e.getClickCount() == 2) {
                    int index = list.locationToIndex(e.getPoint());
                    Activity activity = listModel.get(index);
                    MainFrame.showConfigureDialog(activity.getProperties());
                }
            }
        });
    }

    public void startSelectedActivities() {
        List selectedValues=getSelectedValuesList();
        if (selectedValues.size()>0) {
            for (Object activity : selectedValues) {
                ((Activity)activity).start();
            }
        } else {
            for (Activity activity : activities) {
                activity.start();
            }
        }
    }

    public void registerTaskListener(TaskListener listener) {
        taskListeners.add(listener);
        for (Activity activity : activities) {
            activity.getTaskManager().registerTaskListener(listener);
        }
    }

    public void registerProgressListener(ProgressListener listener) {
        progressListeners.add(listener);
        for (Activity activity : activities) {
            activity.getTaskManager().registerProgressListener(listener);
        }
    }

    public void addActivity(Activity activity) {
        activities.add(activity);
        listModel.addElement(activity);
        for (TaskListener taskListener : taskListeners) {
            activity.getTaskManager().registerTaskListener(taskListener);
        }
        for (ProgressListener progressListener : progressListeners) {
            activity.getTaskManager().registerProgressListener(progressListener);
        }
    }

    public void deleteSelectedActivities() {
        List selectedValues=getSelectedValuesList();
        for (Object o : selectedValues) {
            Activity activity=(Activity)o;
            activity.deleteRecordFile();
            activity.getTaskManager().clearTasks();
            listModel.removeElement(activity);
            this.activities.remove(activity);
        }
    }
}
