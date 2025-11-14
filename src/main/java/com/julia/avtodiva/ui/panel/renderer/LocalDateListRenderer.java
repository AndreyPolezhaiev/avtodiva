package com.julia.avtodiva.ui.panel.renderer;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class LocalDateListRenderer extends DefaultListCellRenderer {
    private final DateTimeFormatter formatter =
            DateTimeFormatter.ofPattern("EEEE, dd.MM.yyyy", new Locale("uk", "UA"));

    @Override
    public Component getListCellRendererComponent(JList<?> list,
                                                  Object value,
                                                  int index,
                                                  boolean isSelected,
                                                  boolean cellHasFocus) {
        JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

        if (value instanceof LocalDate ld) {
            label.setText(ld.format(formatter));
        }

        return label;
    }
}
