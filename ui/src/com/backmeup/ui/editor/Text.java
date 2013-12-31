package com.backmeup.ui.editor;

import schema.ConfigureItem;

import javax.swing.*;
import java.util.Properties;

public class Text extends InputBasedEditor {
    JTextField field;

    public Text(JDialog owner, String provider, final ConfigureItem item, Properties inputValues) {
        super(owner, provider, item, inputValues);
        field = new JTextField();
        field.setColumns(item.getSize());
        field.getDocument().addDocumentListener(new UpdateValueDocumentListener(item.getName(), field));
        add(field);
    }

    public JTextField getField() {
        return field;
    }

}
