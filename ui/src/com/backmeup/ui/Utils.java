package com.backmeup.ui;

import org.apache.log4j.Logger;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.text.DecimalFormat;

public class Utils {
    static Logger logger = Logger.getLogger(BackmeupConsole.class);
    private static DecimalFormat sizeFormatter = new DecimalFormat("#.00");
    static DecimalFormat speedFormatter = new DecimalFormat("#.##");

    public static ImageIcon createImageIcon(String path, int size) {
        java.net.URL imgURL = MainFrame.class.getResource(path);
        BufferedImage image = null;
        try {
            image = ImageIO.read(imgURL);
            BufferedImage resizedImage = resize(image, size, size);
            return new ImageIcon(resizedImage);
        } catch (IOException e) {
            logger.error("Can't read image:" + imgURL, e);
        }
        return null;
    }

    public static BufferedImage resize(BufferedImage image, int width, int height) {
        BufferedImage bi = new BufferedImage(width, height, BufferedImage.TRANSLUCENT);
        Graphics2D g2d = bi.createGraphics();
        g2d.addRenderingHints(new RenderingHints(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY));
        g2d.drawImage(image, 0, 0, width, height, null);
        g2d.dispose();
        return bi;
    }

    public static String getFileSize(long size) {
        if (size == 0) {
            return "0KB";
        }
        float s = size;
        s = s / 1024;
        if (s < 1000) {
            return sizeFormatter.format(s) + "KB";
        }
        s = s / 1024;
        if (s < 1000) {
            return sizeFormatter.format(s) + "MB";
        }
        s = s / 1024;
        return sizeFormatter.format(s) + "GB";
    }

    public static String getSpeed(float kbs) {
         return speedFormatter.format(kbs) + "KB/S";
    }

    public static String getLeftTime(long seconds) {
        if (seconds==Long.MAX_VALUE) return UIContext.getResource("endless");
        long diffSeconds = seconds % 60;
        long diffMinutes = seconds / 60 % 60;
        long diffHours = seconds / (60 * 60) % 24;
        long diffDays = seconds / (60 * 60 * 24);
        StringBuffer buffer=new StringBuffer();
        if (diffDays>0) {
            buffer.append(diffDays).append(" "+UIContext.getResource("days")+", ");
        }
        if (diffHours>0) {
            buffer.append(diffHours).append(" "+UIContext.getResource("hours")+", ");
        }
        if (diffMinutes>0) {
            buffer.append(diffMinutes).append(" "+UIContext.getResource("minutes")+", ");
        }
        buffer.append(diffSeconds).append(" "+UIContext.getResource("seconds"));
        return buffer.toString();
    }

    public static void alignLabels(java.util.List<JLabel> labels) {
        int maxWidth = 0;
        for (JLabel label : labels) {
            int width = label.getPreferredSize().width;
            if (width > maxWidth) {
                maxWidth = width;
            }
            if (label.getFont().getClass().getName().startsWith("javax.swing")) {
            }
        }
        for (JLabel label : labels) {
            label.setMinimumSize(new Dimension(maxWidth, label.getPreferredSize().height));
            label.setPreferredSize(new Dimension(maxWidth, label.getPreferredSize().height));
            label.setFont(UIContext.systemFont);
        }
    }

}
