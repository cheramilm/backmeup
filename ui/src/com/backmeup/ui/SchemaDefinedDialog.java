package com.backmeup.ui;

import com.backmeup.ui.editor.BaseEditor;
import schema.ConfigureItem;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class SchemaDefinedDialog extends JDialog {
    protected Properties inputValues = new Properties();
    int result;
    protected Map<ConfigureItem, BaseEditor> editorMap = new HashMap<ConfigureItem, BaseEditor>();
    protected java.util.List<JLabel> labels = new ArrayList<JLabel>();


    public SchemaDefinedDialog(Frame owner, String title) {
        super(owner, title, true);
        postSet();
    }

    public SchemaDefinedDialog(Dialog owner, String title) {
        super(owner, title, true);
        postSet();
    }

    public SchemaDefinedDialog(Frame owner, String title, Properties inputValues) {
        super(owner,title, true);
        this.inputValues.putAll(inputValues);
        postSet();
    }

    public SchemaDefinedDialog(Dialog owner, String title, Properties inputValues) {
        super(owner,title, true);
        this.inputValues.putAll(inputValues);
        postSet();
    }

    public void addShowAdvanceConfigure(JPanel contentPane) {
        contentPane.add(new ShowAdvanceConfigure(this),0);
    }

    public void alignLabels() {
        Utils.alignLabels(labels);
    }

    public Properties getInputValues() {
        return inputValues;
    }

    public void setShowAdvanceConfigure(boolean showAdvanceConfigure) {
        for (Map.Entry<ConfigureItem, BaseEditor> entry : editorMap.entrySet()) {
            if (entry.getKey().isAdvanced()) {
                if (showAdvanceConfigure) {
                    entry.getValue().setVisible(true);
                } else {
                    entry.getValue().setVisible(false);
                }
            }
        }
        pack();
    }

    private void postSet() {
        resetCloseEvent();
    }

    private void resetCloseEvent() {
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {
                result = JOptionPane.NO_OPTION;
            }
        });
    }

    protected String getValue(ConfigureItem item) {
        String result = inputValues.getProperty(item.getName());
        if (result == null) {
            result = (String) item.getDefaultValue();
        }
        return result;
    }

    protected boolean validateInput() {
        boolean result = true;
        for (Map.Entry<ConfigureItem, BaseEditor> entry : editorMap.entrySet()) {
            ConfigureItem item = entry.getKey();
            String value = inputValues.getProperty(item.getName());
            String validResult = item.isValidInput(value);
            if (validResult != null) {
                entry.getValue().highlightWithError(validResult);
                result = false;
            }
        }
        return result;
    }

    protected void addFootPart(JPanel contentPane, String name) {
        addShowAdvanceConfigure(contentPane);
        JPanel buttonPane = new JPanel();
        buttonPane.setAlignmentX(Component.LEFT_ALIGNMENT);
        buttonPane.setLayout(new FlowLayout(FlowLayout.CENTER));
        JButton okButton = new JButton(name);
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (validateInput()) {
                    SchemaDefinedDialog.this.dispose();
                    result = JOptionPane.YES_OPTION;
                } else {
                    JOptionPane.showMessageDialog(SchemaDefinedDialog.this,
                            "Input is not valid, please fix first.",
                            "Input error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        buttonPane.add(okButton);
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                SchemaDefinedDialog.this.dispose();
                result = JOptionPane.NO_OPTION;
            }
        });
        buttonPane.add(cancelButton);
        contentPane.add(buttonPane);
    }

    protected BaseEditor createEditor(String provider, final ConfigureItem item, String value) {
        String editor = this.getClass().getPackage().getName() + ".editor." + item.getEditor();
        try {
            Class<?> c = Class.forName(editor);
            Class<?>[] types = new Class[] { JDialog.class, String.class, ConfigureItem.class, Properties.class };
            Constructor<?> constructor = c.getConstructor(types);
            BaseEditor realEditor = (BaseEditor) constructor.newInstance(this, provider, item, inputValues);
            realEditor.setValue(value);
            editorMap.put(item, realEditor);
            labels.add(realEditor.getLabel());
            return realEditor;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(SchemaDefinedDialog.this,
                    "Can't init UI component for:" + editor,
                    "Fatal error",
                    JOptionPane.ERROR_MESSAGE);
        }
        return null;
    }

    public int getResult() {
        return result;
    }
}
