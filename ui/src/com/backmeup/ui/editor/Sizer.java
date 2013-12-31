package com.backmeup.ui.editor;

import schema.ConfigureItem;

import javax.swing.*;
import java.util.Properties;

public class Sizer extends BaseEditor {
    JSlider sizer;

    public Sizer(JDialog owner, String provider, final ConfigureItem item, Properties inputValues) {
        super(owner, provider, item, inputValues);
        sizer = new JSlider(JSlider.HORIZONTAL,
                Integer.parseInt(item.getMinValue()), Integer.parseInt(item.getMaxValue()), Integer.parseInt(item.getMinValue()));
        sizer.setMajorTickSpacing(255);
        sizer.setPaintTicks(true);
        sizer.setPaintLabels(true);
        add(sizer);
        if (item.getUnit() != null)
            add(new JLabel(item.getUnit()));
    }

    public void setValue(String value) {
        if (value != null) {
            sizer.setValue(Integer.parseInt(value));
        }
    }

}
