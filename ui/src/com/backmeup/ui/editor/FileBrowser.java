package com.backmeup.ui.editor;

import com.backmeup.ui.UIContext;
import schema.ConfigureItem;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Properties;

public class FileBrowser extends BaseEditor {
    final JTextField textField;
    final JFileChooser fileChooser;

    public FileBrowser(final JDialog owner, String provider, final ConfigureItem item, final Properties inputValues) {
        super(owner, provider, item, inputValues);
        textField = new JTextField(item.getSize());
        textField.setEditable(false);
        add(textField);
        fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        JButton button = new JButton(UIContext.getResource("editor.FileBrowser.Select"));
        add(button);
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int returnVal = fileChooser.showOpenDialog(owner);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File file = fileChooser.getSelectedFile();
                    textField.setText(file.getAbsolutePath());
                    inputValues.put(item.getName(), file.getAbsolutePath());
                }
            }
        });
    }

    public void setValue(String value) {
        if (value != null) {
            textField.setText(value);
            File f = new File(value);
            fileChooser.setCurrentDirectory(f.getParentFile());
        }
    }

}
