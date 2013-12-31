package com.backmeup.ui.editor;

import com.backmeup.provider.ProviderSchema;
import com.backmeup.ui.ProviderConfigureDialog;
import com.backmeup.ui.Utils;
import schema.ConfigureItem;
import schema.UISchema;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Properties;

public class StorageProvider extends BaseEditor {
    final JComboBox<String> providerList;
    final JPanel rootPanel = new JPanel();
    final JPanel configurePanel = new JPanel();

    protected void initEditor() {
        setAlignmentX(Component.LEFT_ALIGNMENT);
        setLayout(new BorderLayout());
    }

    protected JPanel getRootPanel() {
        return rootPanel;
    }

    private void resetPreviewPanel() {
        String newProvider = (String) providerList.getSelectedItem();
        java.util.List<ConfigureItem> items;
        configurePanel.removeAll();
        items = UISchema.getInstance().getProviderConfigure(newProvider).getConfigureItems();
        java.util.List<JLabel> labels=new ArrayList<JLabel>();
        for (ConfigureItem i : items) {
            if (inputValues.getProperty(i.getName()) == null && i.getDefaultValue() != null) {
                inputValues.put(i.getName(), i.getDefaultValue());
            }
            String currentValue = inputValues.getProperty(i.getName());
            if ((!i.isAdvanced()) || (currentValue != null && !currentValue.equals(i.getDefaultValue()))) {
                JPanel valuePanel = new JPanel();
                valuePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
                valuePanel.setLayout(new FlowLayout(FlowLayout.LEADING));
                String v= i.getDisplayValue();
                if (v==null) v=inputValues.getProperty(i.getName());
                JLabel value = new JLabel(v);
                JLabel label=getLabel(i);
                labels.add(label);
                valuePanel.add(label);
                valuePanel.add(value);
                if (i.getUnit() != null) {
                    valuePanel.add(new JLabel(i.getUnit()));
                }
                configurePanel.add(valuePanel);
            }
            Utils.alignLabels(labels);
        }
        configurePanel.validate();
        owner.pack();
    }

    public StorageProvider(final JDialog owner, String provider, final ConfigureItem item, final Properties inputValues) {
        super(owner, provider, item, inputValues);
        getRootPanel().add(label);
        rootPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        rootPanel.setLayout(new FlowLayout(FlowLayout.LEADING));
        providerList = new JComboBox<String>((String[])ProviderSchema.getStorageProviders().toArray());
        rootPanel.add(providerList);
        providerList.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                resetPreviewPanel();
                inputValues.put(item.getName(), providerList.getSelectedItem());
            }
        });
        JButton button = new JButton("Configure...");
        rootPanel.add(button);
        add(rootPanel, BorderLayout.CENTER);
        configurePanel.setLayout(new BoxLayout(configurePanel, BoxLayout.Y_AXIS));
        configurePanel.setBorder(new EmptyBorder(5, 40, 5, 20));
        add(configurePanel, BorderLayout.SOUTH);
        resetPreviewPanel();
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ProviderConfigureDialog dialog = new ProviderConfigureDialog(owner, "Configure " + providerList.getSelectedItem() + " Storage", UISchema.getInstance().getProviderConfigure((String) providerList.getSelectedItem()),inputValues);
                dialog.alignLabels();
                dialog.setShowAdvanceConfigure(false);
                dialog.setLocationRelativeTo(owner);
                dialog.setVisible(true);
                if (dialog.getResult() == JOptionPane.YES_OPTION) {
                    inputValues.putAll(dialog.getInputValues());
                    resetPreviewPanel();
                }
            }
        });
    }

    public void setValue(String value) {
        if (value != null) {
            providerList.setSelectedItem(value);
        }
    }
}
