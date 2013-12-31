package com.backmeup.ui;

import java.awt.*;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class UIContext {
    public static Locale currentLocale=Locale.CHINESE;
    private static final ResourceBundle messages=ResourceBundle.getBundle("com.backmeup.ui.MessagesBundle", currentLocale);
    public static Font systemFont;

    public static String getResource(String name) {
        return messages.getString(name);
    }

    public static String getNullableResource(String name) {
        String result="";
        try {
           result=messages.getString(name);
        } catch (MissingResourceException e) {

        }
        return result;
    }


    public static Font getSystemFont() {
        return systemFont;
    }

    public static void setSystemFont(Font systemFont) {
        UIContext.systemFont = systemFont;
    }
}
