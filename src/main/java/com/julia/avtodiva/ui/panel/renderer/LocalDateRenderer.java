package com.julia.avtodiva.ui.panel.renderer;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class LocalDateRenderer extends DefaultTableCellRenderer {
    private final DateTimeFormatter formatter =
            DateTimeFormatter.ofPattern("EEEE, dd.MM.yyyy", new Locale("uk", "UA"));

    @Override
    protected void setValue(Object value) {
        if (value instanceof LocalDate ld) {
            setText(ld.format(formatter));
        } else {
            super.setValue(value);
        }
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus,
                                                   int row, int column) {
        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        // Чтобы колонка была шире
        table.getColumnModel().getColumn(column).setPreferredWidth(180);
        return c;
    }
}
