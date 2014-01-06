package com.backmeup.ui.editor;

import com.backmeup.provider.ProviderSchema;
import schema.ConfigureItem;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Properties;

public class List extends BaseEditor {
    final JComboBox list;

    public List(JDialog owner, String provider, final ConfigureItem item, final Properties inputValues) {
        super(owner, provider, item, inputValues);
        list = new JComboBox(ProviderSchema.getProvider(provider).getProperty(item.getName()).getAllowValues().toArray());
        list.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                inputValues.put(item.getName(), list.getSelectedItem());
            }
        });
        add(list);
    }

    public void setValue(String value) {
        if (value != null) {
            list.setSelectedItem(value);
        }
    }

}
