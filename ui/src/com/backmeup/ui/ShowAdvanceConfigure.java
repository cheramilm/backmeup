package com.backmeup.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

public class ShowAdvanceConfigure extends JPanel {
    final JCheckBox checkbox;
    final SchemaDefinedDialog owner;
    public ShowAdvanceConfigure(final SchemaDefinedDialog owner) {
        this.owner=owner;
        setAlignmentX(Component.LEFT_ALIGNMENT);
        setLayout(new FlowLayout(FlowLayout.LEADING));
        checkbox = new JCheckBox("Show Advance Configurations");
        //setNullable(checkbox, item);
        checkbox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                owner.setShowAdvanceConfigure(checkbox.isSelected());
            }
        });
        add(checkbox);
    }
}
