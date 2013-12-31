package com.backmeup.ui;

import com.backmeup.ui.editor.BaseEditor;
import schema.ConfigureItem;
import schema.UISchema;

import javax.swing.*;
import java.awt.*;
import java.util.Properties;

public class ConfigureDialog extends SchemaDefinedDialog {
    public ConfigureDialog(Frame owner, String title) {
        super(owner, title);
        setDefaultValues();
        initContent();
    }

    public ConfigureDialog(Frame owner, String title, Properties inputValues) {
        super(owner, title, inputValues);
        initContent();
    }

    private void initContent() {
        JPanel contentPane = new JPanel();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
        UISchema uiSchema = UISchema.getInstance();
        for (ConfigureItem item : uiSchema.getCommonConfigureItems()) {
            contentPane.add(createEditor(null, item, getValue(item)));
        }
        addFootPart(contentPane, "Add");
        setContentPane(contentPane);
    }

    private void setDefaultValues() {
        UISchema uiSchema = UISchema.getInstance();
        for (ConfigureItem item : uiSchema.getCommonConfigureItems()) {
            Object value = item.getDefaultValue();
            if (value != null) {
                inputValues.put(item.getName(), item.getDefaultValue());
            }
        }
    }

    protected boolean validateInput() {
        boolean result = super.validateInput();
        java.util.List<ConfigureItem> items = UISchema.getInstance().getProviderConfigure(inputValues.getProperty("StorageProvider")).getConfigureItems();
        BaseEditor editor = editorMap.get(UISchema.getInstance().getCommonConfigureItem("StorageProvider"));
        for (ConfigureItem item : items) {
            String value = inputValues.getProperty(item.getName());
            String validResult = item.isValidInput(value);
            if (validResult != null) {
                editor.highlightWithError(validResult);
                result = false;
            }
        }
        return result;
    }
}
