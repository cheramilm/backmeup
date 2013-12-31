package com.backmeup.ui;

import com.backmeup.RecordManager;
import org.apache.log4j.Logger;

import javax.swing.*;

public class BackmeupConsole {
    static Logger logger = Logger.getLogger(BackmeupConsole.class);


    private static void createAndShowGUI() {
        //Create and set up the window.
        MainFrame frame = new MainFrame();
        //frame.pack();
        frame.setVisible(true);
        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                RecordManager.saveAll();
            }
        });
    }

    public static void main(String[] args) {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        try {
            UIManager.put("RootPane.setupButtonVisible", false);
            org.jb2011.lnf.beautyeye.BeautyEyeLNFHelper.launchBeautyEyeLNF();
        } catch (Exception e) {
            logger.warn("Can't init BeautyEye look and feel.", e);
        }
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }
}
