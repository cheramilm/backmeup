package com.backmeup.ui.editor;

import schema.ConfigureItem;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.util.Properties;

public abstract class InputBasedEditor extends BaseEditor {
    public InputBasedEditor(JDialog owner, String provider, final ConfigureItem item, Properties inputValues) {
        super(owner, provider, item, inputValues);
    }

    public class UpdateValueDocumentListener implements DocumentListener {
        final JTextField field;
        String name;

        public UpdateValueDocumentListener(String name, JTextField field) {
            this.name = name;
            this.field = field;
        }

        public void insertUpdate(DocumentEvent e) {
            inputValues.put(name, field.getText());
        }

        public void removeUpdate(DocumentEvent e) {
            inputValues.put(name, field.getText());
        }

        public void changedUpdate(DocumentEvent e) {
            inputValues.put(name, field.getText());
        }
    }
    public void setValue(String value) {
        if (value != null) {
            getField().setText(value);
        }
    }

    public abstract JTextField getField();

}
