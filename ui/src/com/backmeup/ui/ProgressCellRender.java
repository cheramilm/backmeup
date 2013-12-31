package com.backmeup.ui;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

public class ProgressCellRender extends JProgressBar implements TableCellRenderer {

    public ProgressCellRender() {
        setStringPainted(true);
    }

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        int progress = 0;
        if (value instanceof Float) {
            progress = Math.round(((Float) value) * 100f);
            setValue(progress);
        } else if (value instanceof Integer) {
            progress = (Integer) value;
            setValue(progress);
        } else if (value instanceof String) {
           String[] values=((String) value).split(",");
           setValue(Math.round((Float.parseFloat(values[0])) * 100f));
           setString(getValue()+"%("+Utils.getSpeed(Float.parseFloat(values[1]))+")");
        }
        return this;
    }
}
