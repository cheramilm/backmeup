package com.backmeup.ui.editor;

import com.backmeup.ui.UIContext;
import schema.ConfigureItem;

import javax.swing.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Properties;

public class Checkbox extends BaseEditor {
    final JCheckBox checkbox;

    public Checkbox(JDialog owner, String provider, final ConfigureItem item, final Properties inputValues) {
        super(owner, provider, item, inputValues);
        checkbox = new JCheckBox();
        //setNullable(checkbox, item);
        checkbox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                inputValues.put(item.getName(), checkbox.isSelected());
            }
        });
        add(checkbox);
        add(new JLabel(UIContext.getNullableResource("editor."+item.getName()+".description")));
    }

    public void setValue(String value) {
        if (value != null && value.equals("true")) {
            checkbox.setSelected(true);
        } else {
            checkbox.setSelected(false);
        }
    }

}
