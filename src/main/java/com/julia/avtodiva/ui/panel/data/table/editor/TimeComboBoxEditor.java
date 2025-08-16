package com.julia.avtodiva.ui.panel.data.table.editor;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class TimeComboBoxEditor extends DefaultCellEditor {
    private final JComboBox<String> combo;
    private final DateTimeFormatter tf = DateTimeFormatter.ofPattern("HH:mm");

    public TimeComboBoxEditor(int[][] hours) {
        super(new JComboBox<>());
        this.combo = (JComboBox<String>) getComponent();
        this.combo.setEditable(true); // можно выбирать из списка и печатать руками
        // заполняем дефолтные слоты
        for (int[] h : hours) {
            LocalTime t = LocalTime.of(h[0], h[1]);
            combo.addItem(tf.format(t));
        }
        // при выборе из списка сразу коммитим значение в модель
        combo.addActionListener(e -> {
            if (combo.isPopupVisible()) {
                SwingUtilities.invokeLater(this::stopCellEditing);
            }
        });
        // при потере фокуса тоже коммитим
        combo.getEditor().getEditorComponent().addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusLost(java.awt.event.FocusEvent e) { stopCellEditing(); }
        });
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        // показываем текущее значение ячейки
        combo.setSelectedItem(value instanceof String ? (String) value : value != null ? value.toString() : "");
        return combo;
    }

    @Override
    public Object getCellEditorValue() {
        // всегда возвращаем строку вида HH:mm — именно такую строку ты уже парсишь в setValueAt
        Object v = combo.getEditor().getItem();
        String s = v == null ? "" : v.toString().trim();
        // необязательная нормализация: если пользователь ввёл, например, "8:0" → "08:00"
        try {
            if (!s.isEmpty()) s = LocalTime.parse(s.length() == 4 ? "0" + s : s, DateTimeFormatter.ofPattern("H:mm")).format(tf);
        } catch (Exception ignore) { /* если не распарсилось — отдаём как есть, setValueAt выбросит и покажет твоё сообщение */ }
        return s;
    }
}
