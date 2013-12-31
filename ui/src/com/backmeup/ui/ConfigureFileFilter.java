package com.backmeup.ui;

import com.backmeup.Activity;

import java.io.File;
import java.io.FilenameFilter;

public class ConfigureFileFilter implements FilenameFilter {
    public boolean accept(File dir, String name) {
        String lowercaseName = name.toLowerCase();
        if (lowercaseName.startsWith(Activity.ACTIVITY_FILE_PREFIX) && lowercaseName.endsWith(Activity.ACTIVITY_FILE_EXT)) {
            return true;
        } else {
            return false;
        }
    }
}
