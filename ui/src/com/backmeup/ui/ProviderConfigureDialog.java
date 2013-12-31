package com.backmeup.ui;

import schema.ConfigureItem;
import schema.ProviderConfigure;

import javax.swing.*;
import java.awt.*;
import java.util.Properties;

public class ProviderConfigureDialog extends SchemaDefinedDialog {
    ProviderConfigure configure;

    public ProviderConfigureDialog(Dialog owner, String title, ProviderConfigure configure) {
        super(owner, title);
        this.configure = configure;
        setDefaultValues();
        initContent();
    }

    public ProviderConfigureDialog(Dialog owner, String title, ProviderConfigure configure,Properties inputValues) {
        super(owner,title,inputValues);
        this.configure = configure;
        initContent();
    }

    private void initContent() {
        JPanel contentPane = new JPanel();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
        for (ConfigureItem item : configure.getConfigureItems()) {
            contentPane.add(createEditor(configure.getName(), item,getValue(item)));
        }
        addFootPart(contentPane, "OK");
        setContentPane(contentPane);
    }

    private void setDefaultValues() {
        for (ConfigureItem item : configure.getConfigureItems()) {
            Object value=item.getDefaultValue();
            if (value!=null) {
                inputValues.put(item.getName(),item.getDefaultValue());
            }
        }
    }

}
