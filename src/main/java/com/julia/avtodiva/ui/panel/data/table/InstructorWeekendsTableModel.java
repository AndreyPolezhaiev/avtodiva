// InstructorWeekendsTableModel.java
package com.julia.avtodiva.ui.panel.data.table;

import com.julia.avtodiva.model.Instructor;
import com.julia.avtodiva.model.Weekend;

import javax.swing.table.AbstractTableModel;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class InstructorWeekendsTableModel extends AbstractTableModel {
    private static final String[] COLUMNS = {"✓", "Інструктор", "Дата", "Час з", "Час до"};

    private final Instructor instructor;
    private final List<Weekend> weekends;
    private final List<Boolean> selected;

    public InstructorWeekendsTableModel(Instructor instructor) {
        this.instructor = instructor;
        List<Weekend> src = instructor.getWeekends() != null ? instructor.getWeekends() : new ArrayList<>();
        this.weekends = new ArrayList<>(src);

        this.weekends.sort(
                Comparator.comparing(Weekend::getDay)
                        .thenComparing(Weekend::getTimeFrom, Comparator.nullsLast(Comparator.naturalOrder()))
        );

        this.selected = new ArrayList<>(weekends.size());
        for (int i = 0; i < weekends.size(); i++) selected.add(false);
    }

    @Override
    public int getRowCount() {
        return weekends.size();
    }

    @Override
    public int getColumnCount() {
        return COLUMNS.length;
    }

    @Override
    public String getColumnName(int column) {
        return COLUMNS[column];
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return switch (columnIndex) {
            case 0 -> Boolean.class;
            case 2 -> LocalDate.class;
            case 3, 4 -> LocalTime.class;
            default -> String.class;
        };
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        if (columnIndex == 0) return true;
        if (columnIndex == 1) return false;
        return Boolean.TRUE.equals(selected.get(rowIndex));
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Weekend w = weekends.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> selected.get(rowIndex);
            case 1 -> instructor != null && instructor.getName() != null ? instructor.getName() : "";
            case 2 -> w.getDay() != null ? w.getDay() : "";
            case 3 -> w.getTimeFrom() != null ? w.getTimeFrom() : "";
            case 4 -> w.getTimeTo() != null ? w.getTimeTo() : "";
            default -> null;
        };
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        Weekend w = weekends.get(rowIndex);
        try {
            switch (columnIndex) {
                case 0 -> {
                    if (aValue instanceof Boolean b) {
                        selected.set(rowIndex, b);
                    }
                }
                case 2 -> {
                    if (aValue instanceof LocalDate localDate) {
                        w.setDay(localDate);
                    }
                }
                case 3 -> {
                    if (aValue instanceof LocalTime timeFrom) {
                        w.setTimeFrom(timeFrom);
                    }
                }
                case 4 -> {
                    if (aValue instanceof LocalTime timeTo) {
                        w.setTimeTo(timeTo);
                    }
                }
                // column 1 (Инструктор) намеренно не обрабатываем
            }
            fireTableCellUpdated(rowIndex, columnIndex);
        } catch (Exception ex) {
            System.err.println("Помилка при редагуванні комірки: " + ex.getMessage());
        }
    }

    public List<Weekend> getSelectedWeekends() {
        List<Weekend> out = new ArrayList<>();
        for (int i = 0; i < weekends.size(); i++) if (Boolean.TRUE.equals(selected.get(i))) out.add(weekends.get(i));
        return out;
    }

    public void selectAll(boolean select) {
        for (int i = 0; i < weekends.size(); i++) {
            selected.set(i, select);
        }
        fireTableDataChanged();
    }
}
