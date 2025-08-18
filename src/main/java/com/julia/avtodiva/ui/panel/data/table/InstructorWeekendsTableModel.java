// InstructorWeekendsTableModel.java
package com.julia.avtodiva.ui.panel.data.table;

import com.julia.avtodiva.model.Instructor;
import com.julia.avtodiva.model.Weekend;

import javax.swing.table.AbstractTableModel;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class InstructorWeekendsTableModel extends AbstractTableModel {
    private static final String[] COLUMNS = {"✓", "Инструктор", "Дата", "Время с", "Время до"};

    private final Instructor instructor;
    private final List<Weekend> weekends;
    private final List<Boolean> selected;
    private final DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private final DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm");

    public InstructorWeekendsTableModel(Instructor instructor) {
        this.instructor = instructor;
        List<Weekend> src = instructor.getWeekends() != null ? instructor.getWeekends() : new ArrayList<>();
        this.weekends = new ArrayList<>(src);
        this.selected = new ArrayList<>(weekends.size());
        for (int i = 0; i < weekends.size(); i++) selected.add(false);
    }

    @Override public int getRowCount() { return weekends.size(); }
    @Override public int getColumnCount() { return COLUMNS.length; }
    @Override public String getColumnName(int column) { return COLUMNS[column]; }
    @Override public Class<?> getColumnClass(int columnIndex) { return columnIndex == 0 ? Boolean.class : String.class; }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        if (columnIndex == 0) return true;      // чекбокс
        if (columnIndex == 1) return false;     // имя инструктора нередактируемо
        return Boolean.TRUE.equals(selected.get(rowIndex));
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Weekend w = weekends.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> selected.get(rowIndex);
            case 1 -> instructor != null && instructor.getName() != null ? instructor.getName() : "";
            case 2 -> w.getDay() != null ? w.getDay().format(dateFmt) : "";
            case 3 -> w.getTimeFrom() != null ? w.getTimeFrom().format(timeFmt) : "";
            case 4 -> w.getTimeTo() != null ? w.getTimeTo().format(timeFmt) : "";
            default -> null;
        };
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        Weekend w = weekends.get(rowIndex);
        try {
            switch (columnIndex) {
                case 0 -> { if (aValue instanceof Boolean b) selected.set(rowIndex, b); }
                case 2 -> { String s = aValue == null ? "" : aValue.toString().trim(); w.setDay(s.isEmpty() ? null : LocalDate.parse(s, dateFmt)); }
                case 3 -> { String s = aValue == null ? "" : aValue.toString().trim(); w.setTimeFrom(s.isEmpty() ? null : LocalTime.parse(s, timeFmt)); }
                case 4 -> { String s = aValue == null ? "" : aValue.toString().trim(); w.setTimeTo(s.isEmpty() ? null : LocalTime.parse(s, timeFmt)); }
                // column 1 (Инструктор) намеренно не обрабатываем
            }
            fireTableCellUpdated(rowIndex, columnIndex);
        } catch (Exception ex) {
            System.err.println("Bad value: " + ex.getMessage());
        }
    }

    public List<Weekend> getSelectedWeekends() {
        List<Weekend> out = new ArrayList<>();
        for (int i = 0; i < weekends.size(); i++) if (Boolean.TRUE.equals(selected.get(i))) out.add(weekends.get(i));
        return out;
    }
}
