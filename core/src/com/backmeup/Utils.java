package com.backmeup;

import java.io.File;

public class Utils {
    public static String getRelativeFilePath(String rootDirectory, File file) {
        File root = new File(rootDirectory);
        String f = root.getAbsolutePath();
        String filePath = file.getAbsolutePath();
        if (filePath.length() <= f.length()) {
            return "";
        } else {
            return filePath.substring(f.length() + 1);
        }
    }

    public static String getExtensionName(String name) {
        int index = name.lastIndexOf(".");
        if (index >= 0) {
            return name.substring(index + 1).toLowerCase();
        } else {
            return "";
        }
    }

    public static int getDirectoryLevel(String rootDirectory, File file) {
        String relativePath = getRelativeFilePath(rootDirectory, file);
        int result = 0;
        char[] chars = relativePath.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] == '/' || chars[i] == '\\') {
                result++;
            }
        }
        return result;
    }

    public static float getSpeedKBS(long bytes, long milliseconds) {
        float mb = bytes;
        mb = mb / 1024;
        float second = milliseconds;
        second = second / 1000;
        return mb / second;
    }

    public static String getTargetPath(String rootPath, String relativePath) {
        return rootPath + "/" + relativePath.replace('\\', '/');
    }

    public static Object getValue(String type, String value) {
        if ("String".equals(type)) {
            return value;
        } else if ("Int".equals(type)) {
            return Integer.parseInt(value);
        } else if ("Long".equals(type)) {
            return Long.parseLong(value);
        } else if ("Boolean".equals(type)) {
            if ("false".equals(value)) {
                return false;
            } else if ("true".equals(value)) {
                return true;
            } else {
                throw new RuntimeException("Invalid value for boolean type:" + value);
            }
        } else {
            return value;
        }
    }

    public static void sleep(long time) {
        /*try {
            Thread.sleep(time);
        } catch (InterruptedException e) {

        } */
    }


}
