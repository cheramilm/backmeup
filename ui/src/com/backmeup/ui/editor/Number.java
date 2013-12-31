package com.backmeup.ui.editor;

import schema.ConfigureItem;

import javax.swing.*;
import java.text.NumberFormat;
import java.util.Properties;

public class Number extends InputBasedEditor {
    final JFormattedTextField field;

    public Number(JDialog owner, String provider, final ConfigureItem item, Properties inputValues) {
        super(owner, provider, item, inputValues);
        field = new JFormattedTextField(NumberFormat.getNumberInstance());
        field.setColumns(item.getSize());
        field.getDocument().addDocumentListener(new UpdateValueDocumentListener(item.getName(), field));
        add(field);
        if (item.getUnit() != null && item.getUnit().length() > 0) {
            add(new JLabel(item.getUnit()));
        }
    }

    public JTextField getField() {
        return field;
    }

}
