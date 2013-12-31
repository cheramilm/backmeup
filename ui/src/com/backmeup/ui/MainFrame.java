package com.backmeup.ui;

import com.backmeup.Activity;
import com.backmeup.ApplicationException;
import com.backmeup.task.TaskEventType;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Properties;

public class MainFrame extends JFrame {
    static Logger logger = Logger.getLogger(MainFrame.class);
    static MainFrame instance;
    Container rootContainer;
    JToolBar toolBar;
    JButton addButton;
    JButton deleteButton;
    JButton saveButton;
    JButton startButton;
    JButton pauseButton;
    JButton stopButton;
    JButton settingButton;
    JTabbedPane taskPane;
    StatusBar statusBar;
    ActivityList activityList;
    JSplitPane mainContent;
    ConfigureDialog configureDialog;
    StartListener startListener = new StartListener();
    AddConfigureListener configureListener = new AddConfigureListener();
    DeleteConfigureListener deleteConfigureListener = new DeleteConfigureListener();

    public MainFrame() {
        setTitle(UIContext.getResource("application.title"));
        UIContext.setSystemFont(this.getFont());
        rootContainer = getRootPane().getContentPane();
        rootContainer.setLayout(new BorderLayout());
        addToolbar();
        addActivityList();
        addTaskPane();
        addMainContent();
        addStatusBar();
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        instance = this;
    }

    private void addMainContent() {
        mainContent = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(activityList), taskPane);
        mainContent.setOneTouchExpandable(true);
        mainContent.setDividerLocation(200);

//Provide minimum sizes for the two components in the split pane
        Dimension minimumSize = new Dimension(100, 50);
        //listScrollPane.setMinimumSize(minimumSize);
        //pictureScrollPane.setMinimumSize(minimumSize);
        rootContainer.add(mainContent, BorderLayout.CENTER);
    }

    private void addActivityList() {
        activityList = new ActivityList();
    }

    private void addToolbar() {
        toolBar = new JToolBar();
        addButton = addButton("/images/add.png");
        addButton.addActionListener(configureListener);
        deleteButton = addButton("/images/delete.png");
        deleteButton.addActionListener(deleteConfigureListener);
        saveButton = addButton("/images/save.png");
        startButton = addButton("/images/start.png");
        startButton.addActionListener(startListener);
        pauseButton = addButton("/images/pause.png");
        stopButton = addButton("/images/stop.png");
        settingButton = addButton("/images/setting.png");
        rootContainer.add(toolBar, BorderLayout.PAGE_START);
    }

    private void start() {
        activityList.startSelectedActivities();
    }

    private JButton addButton(String path) {
        ImageIcon icon = Utils.createImageIcon(path, 32);
        JButton button = new JButton(icon);
        toolBar.add(button);
        return button;
    }

    private void addStatusBar() {
        statusBar = new StatusBar(UIContext.getResource("ready"));
        rootContainer.add(statusBar, BorderLayout.PAGE_END);
        activityList.registerTaskListener(statusBar);
        activityList.registerProgressListener(statusBar);
    }

    private void addTaskPane() {
        taskPane = new JTabbedPane();
        taskPane.setTabPlacement(JTabbedPane.BOTTOM);
        addTaskPanel("taskPane.running", new TaskEventType[]{TaskEventType.ExecutingTaskAdded, TaskEventType.ExecutingTaskRemoved,}, true);
        addTaskPanel("taskPane.pending", new TaskEventType[]{TaskEventType.PendingTaskAdded, TaskEventType.PendingTaskRemoved, TaskEventType.ExecutableTaskAdded, TaskEventType.ExecutableTaskRemoved}, true);
        addTaskPanel("taskPane.failed", new TaskEventType[]{TaskEventType.FailedTaskAdded, TaskEventType.FailedTaskRemoved}, false);
        addTaskPanel("taskPane.completed", new TaskEventType[]{TaskEventType.CompletedTaskAdded, TaskEventType.CompletedTaskRemoved}, false);
    }

    private void addTaskPanel(String name, TaskEventType[] eventTypes, boolean useSortedList) {
        TaskTable table = new TaskTable(eventTypes, useSortedList);
        activityList.registerTaskListener(table.getTaskListener());
        if (table.isExecutingTable()) {
            activityList.registerProgressListener(table.getProgressListener());
        }
        JComponent result = new JScrollPane(table);
        taskPane.addTab(UIContext.getResource(name + ".name"), null, result,
                UIContext.getResource(name + ".tooltip"));
    }

    private class StartListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            activityList.startSelectedActivities();
        }
    }

    public static ConfigureDialog showConfigureDialog(Properties inputValues) {
        ConfigureDialog configureDialog = new ConfigureDialog(instance, "Configure Activity", inputValues);
        configureDialog.alignLabels();
        configureDialog.setShowAdvanceConfigure(false);
        configureDialog.setLocationRelativeTo(instance);
        configureDialog.setVisible(true);
        return configureDialog;
    }

    private class DeleteConfigureListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            activityList.deleteSelectedActivities();
        }
    }

    private class AddConfigureListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            configureDialog = new ConfigureDialog(MainFrame.this, "Configure New Activity");
            configureDialog.alignLabels();
            configureDialog.setShowAdvanceConfigure(false);
            configureDialog.setLocationRelativeTo(MainFrame.this);
            configureDialog.setVisible(true);
            if (configureDialog.getResult() == JOptionPane.YES_OPTION) {
                Properties values = configureDialog.getInputValues();
                Activity activity = new Activity();
                try {
                    activity.initFrom(values);
                    activity.init();
                    activity.save(activity.getNextAvailableFile());
                    activityList.addActivity(activity);
                } catch (ApplicationException e1) {
                    JOptionPane.showMessageDialog(MainFrame.this,
                            "Configure error:" + e1.getMessage(),
                            "Fatal error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
}
