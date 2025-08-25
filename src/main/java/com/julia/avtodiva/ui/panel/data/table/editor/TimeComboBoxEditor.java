package com.julia.avtodiva.ui.panel.data.table.editor;

import javax.swing.*;
import java.awt.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class TimeComboBoxEditor extends DefaultCellEditor {
    private final JComboBox<String> combo;
    private final DateTimeFormatter tf = DateTimeFormatter.ofPattern("HH:mm");

    public TimeComboBoxEditor(int[][] hours) {
        super(new JComboBox<>());
        this.combo = (JComboBox<String>) getComponent();
        this.combo.setEditable(true);
        for (int[] h : hours) {
            LocalTime t = LocalTime.of(h[0], h[1]);
            combo.addItem(tf.format(t));
        }
        combo.addActionListener(e -> {
            if (combo.isPopupVisible()) {
                SwingUtilities.invokeLater(this::stopCellEditing);
            }
        });
        combo.getEditor().getEditorComponent().addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusLost(java.awt.event.FocusEvent e) { stopCellEditing(); }
        });
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        combo.setSelectedItem(value instanceof String ? value : value != null ? value.toString() : "");
        return combo;
    }

    @Override
    public Object getCellEditorValue() {
        Object v = combo.getEditor().getItem();
        String s = v == null ? "" : v.toString().trim();
        try {
            if (!s.isEmpty()) {
                return LocalTime.parse(
                        s.length() == 4 ? "0" + s : s,
                        DateTimeFormatter.ofPattern("H:mm")
                );
            }
        } catch (Exception ignore) {}
        return null;
    }
}
