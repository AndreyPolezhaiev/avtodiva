package com.julia.avtodiva.ui.panel.data.table.editor;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import java.awt.*;

public class MultiLineCellEditor extends AbstractCellEditor implements TableCellEditor {
    private final JTextArea area = new JTextArea();
    public MultiLineCellEditor() {
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
    }
    @Override
    public Object getCellEditorValue() { return area.getText(); }
    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        area.setText(value == null ? "" : value.toString());
        area.setFont(table.getFont());
        return new JScrollPane(area); // опционально скролл в редакторе
    }
}
