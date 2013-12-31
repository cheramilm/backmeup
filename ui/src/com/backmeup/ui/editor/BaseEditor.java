package com.backmeup.ui.editor;

import com.backmeup.ui.UIContext;
import schema.ConfigureItem;

import javax.swing.*;
import java.awt.*;
import java.util.Properties;

public class BaseEditor extends JPanel {
    protected String provider;
    protected final ConfigureItem item;
    protected final Properties inputValues;
    protected JLabel label;
    protected JDialog owner;

    public BaseEditor(JDialog owner, String provider, final ConfigureItem item, Properties inputValues) {
        this.owner=owner;
        initEditor();
        this.provider = provider;
        this.item = item;
        this.inputValues = inputValues;
        label = new JLabel(UIContext.getResource("editor."+item.getName()+".name") + ":");
        setNullable(label, item);
        if (getRootPanel() != null) {
            getRootPanel().add(label);
        }
    }

    protected JPanel getRootPanel() {
        return this;
    }

    protected void initEditor() {
        setAlignmentX(Component.LEFT_ALIGNMENT);
        setLayout(new FlowLayout(FlowLayout.LEADING));
    }

    public void highlightWithError(String error) {
        setBorder(BorderFactory.createLineBorder(Color.red));
    }

    public JLabel getLabel(ConfigureItem item) {
        JLabel label = new JLabel(UIContext.getResource("editor."+item.getName()+".name") + ":");
        setNullable(label, item);
        return label;
    }

    public JLabel getLabel() {
        return label;
    }

    public void setNullable(Component component, ConfigureItem item) {
        if (!item.getProperty().isNullable()) {
            Font font = component.getFont();
            Font boldFont = new Font(font.getFontName(), Font.BOLD, font.getSize());
            component.setFont(boldFont);
        }
    }

    public void setValue(String value) {
    }

}
