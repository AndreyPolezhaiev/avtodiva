package com.julia.avtodiva.ui.panel.data.table.editor;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Locale;

public class DateComboBoxEditor extends AbstractCellEditor implements TableCellEditor {
    private final JSpinner spinner;

    public DateComboBoxEditor() {
        // дефолтная дата — сегодня
        LocalDate today = LocalDate.now();
        Date init = Date.from(today.atStartOfDay(ZoneId.systemDefault()).toInstant());

        SpinnerDateModel model = new SpinnerDateModel(init, null, null, java.util.Calendar.DAY_OF_MONTH);
        spinner = new JSpinner(model);

        // формат: "понедельник, 19.08.2025"
        JSpinner.DateEditor editor = new JSpinner.DateEditor(spinner, "EEEE, dd.MM.yyyy");
        java.text.DateFormatSymbols dfs = java.text.DateFormatSymbols.getInstance(new Locale("ru", "UA"));
        editor.getFormat().setDateFormatSymbols(dfs);
        spinner.setEditor(editor);

        spinner.setPreferredSize(new Dimension(200, spinner.getPreferredSize().height));
    }

    @Override
    public Object getCellEditorValue() {
        Date date = (Date) spinner.getValue();
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value,
                                                 boolean isSelected, int row, int column) {
        if (value instanceof LocalDate ld) {
            spinner.setValue(Date.from(ld.atStartOfDay(ZoneId.systemDefault()).toInstant()));
        } else if (value instanceof Date d) {
            spinner.setValue(d);
        }
        return spinner;
    }
}
