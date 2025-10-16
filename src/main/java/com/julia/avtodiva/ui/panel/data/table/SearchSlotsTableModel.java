package com.julia.avtodiva.ui.panel.data.table;

import com.julia.avtodiva.model.ScheduleSlot;
import com.julia.avtodiva.model.Student;
import com.julia.avtodiva.ui.state.AppState;

import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class SearchSlotsTableModel extends AbstractTableModel {
    private final String[] columnNames = AppState.COLUMNS;
    private final List<ScheduleSlot> slots;
    private final List<Boolean> selected;

    private int sortedColumn = -1;
    private boolean ascending = true;

    public SearchSlotsTableModel(List<ScheduleSlot> slots) {
        this.slots = new ArrayList<>(slots);
        this.selected = new ArrayList<>();

        applyCurrentSort();

        for (int i = 0; i < this.slots.size(); i++) {
            selected.add(false);
        }
    }

    @Override
    public int getRowCount() {
        return slots.size();
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return switch (columnIndex) {
            case 0 -> Boolean.class;
            case 1 -> LocalDate.class;
            case 4, 5 -> LocalTime.class;
            default -> String.class;
        };
    }

    public ScheduleSlot getSlotAt(int rowIndex) {
        return slots.get(rowIndex);
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        if (columnIndex == 0) return true;

        return selected.get(rowIndex);
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        ScheduleSlot slot = slots.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> selected.get(rowIndex);
            case 1 -> slot.getDate();
            case 2 -> slot.getInstructor().getName();
            case 3 -> slot.getCar().getName();
            case 4 -> slot.getTimeFrom() != null ? slot.getTimeFrom() : "";
            case 5 -> slot.getTimeTo() != null ? slot.getTimeTo() : "";
            case 6 -> slot.getStudent() != null ? slot.getStudent().getName() : "";
            case 7 -> slot.getDescription();
            case 8 -> slot.getLink();
            default -> null;
        };
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        ScheduleSlot slot = slots.get(rowIndex);

        try {
            switch (columnIndex) {
                case 0 -> {
                    if (aValue instanceof Boolean boolVal) {
                        selected.set(rowIndex, boolVal);
                    }
                }
                case 1 -> { // Дата
                    if (aValue instanceof LocalDate localDate) {
                        slot.setDate(localDate);
                    }
                }
                case 2 -> { // Инструктор
                    if (aValue instanceof String name) {
                        slot.getInstructor().setName(name);
                    }
                }
                case 3 -> { // Машина
                    if (aValue instanceof String name) {
                        slot.getCar().setName(name);
                    }
                }
                case 4 -> { // Время с
                    if (aValue instanceof LocalTime timeFrom) {
                        slot.setTimeFrom(timeFrom);
                    }
                }
                case 5 -> { // Время до
                    if (aValue instanceof LocalTime timeTo) {
                        slot.setTimeTo(timeTo);
                    }
                }
                case 6 -> { // Ученица
                    if (aValue instanceof String name) {
                        if (slot.getStudent() != null) {
                            slot.getStudent().setName(name.trim());
                        } else {
                            Student student = new Student();
                            student.setName(name.trim());
                            slot.setStudent(student);
                        }
                    }
                }
                case 7 -> {
                    if (aValue instanceof String description) {
                        slot.setDescription(description);
                    }
                }
                case 8 -> {
                    if (aValue instanceof String link) {
                        slot.setLink(link);
                    }
                }
            }
            fireTableCellUpdated(rowIndex, columnIndex);
        } catch (Exception e) {
            System.err.println("Помилка при редагуванні комірки: " + e.getMessage());
        }
    }

    public List<ScheduleSlot> getSelectedSlots() {
        List<ScheduleSlot> result = new ArrayList<>();
        for (int i = 0; i < slots.size(); i++) {
            if (selected.get(i)) {
                ScheduleSlot slot = slots.get(i);

                if (slot.getTimeFrom().isAfter(LocalTime.of(18, 0))) {
                    slot.setTimeTo(slot.getTimeFrom().plusHours(2));

                } else {
                    slot.setTimeTo(slot.getTimeFrom().plusHours(3));
                }

                Student student = slot.getStudent();

                if (student != null) {
                    if (!student.getName().isEmpty()) {
                        slot.setBooked(true);
                    }
                } else {
                    slot.setBooked(false);
                }

                result.add(slot);
            }
        }
        return result;
    }

    public void updateSlots(List<ScheduleSlot> newSlots) {
        slots.clear();
        slots.addAll(newSlots);

        applyCurrentSort();

        selected.clear();
        for (int i = 0; i < slots.size(); i++) {
            selected.add(false);
        }
        fireTableDataChanged();
    }

    private void sortSlots() {
        this.slots.sort(
                Comparator.comparing(ScheduleSlot::getDate)
                        .thenComparing(ScheduleSlot::getTimeFrom)
        );
    }

    public void selectAll(boolean select) {
        for (int i = 0; i < slots.size(); i++) {
            selected.set(i, select);
        }
        fireTableDataChanged();
    }

    private void applyCurrentSort() {
        // Если колонка не была выбрана, используем сортировку по умолчанию
        if (sortedColumn == -1) {
            sortSlots(); // Старый метод сортировки по дате/времени
            return;
        }

        Comparator<ScheduleSlot> comparator = switch (sortedColumn) {
            case 1 -> Comparator.comparing(ScheduleSlot::getDate, Comparator.nullsLast(Comparator.naturalOrder()));
            case 2 -> Comparator.comparing(slot -> slot.getInstructor().getName(), String.CASE_INSENSITIVE_ORDER);
            case 3 -> Comparator.comparing(slot -> slot.getCar().getName(), String.CASE_INSENSITIVE_ORDER);
            case 4 -> Comparator.comparing(ScheduleSlot::getTimeFrom, Comparator.nullsLast(Comparator.naturalOrder()));
            case 6 -> Comparator.comparing(slot -> slot.getStudent() != null ? slot.getStudent().getName() : "", String.CASE_INSENSITIVE_ORDER);
            default -> null;
        };

        if (comparator == null) return;

        if (!ascending) {
            comparator = comparator.reversed();
        }

        // Сохраняем выделение при сортировке
        List<SlotSelectionPair> pairs = new ArrayList<>();
        for (int i = 0; i < slots.size(); i++) {
            pairs.add(new SlotSelectionPair(slots.get(i), selected.get(i)));
        }

        pairs.sort(Comparator.comparing(SlotSelectionPair::getSlot, comparator));

        slots.clear();
        selected.clear();
        for (SlotSelectionPair pair : pairs) {
            slots.add(pair.getSlot());
            selected.add(pair.isSelected());
        }
    }

    // --- ИЗМЕНЕНИЕ 2: Упрощаем sortByColumn, теперь он только устанавливает параметры и вызывает applyCurrentSort ---
    public void sortByColumn(int columnIndex) {
        if (columnIndex < 1) return;

        if (sortedColumn == columnIndex) {
            ascending = !ascending;
        } else {
            sortedColumn = columnIndex;
            ascending = true;
        }
        applyCurrentSort();
        fireTableDataChanged();
    }

    // Вспомогательный класс для сохранения выделения
    private static class SlotSelectionPair {
        private final ScheduleSlot slot;
        private final boolean selected;
        public SlotSelectionPair(ScheduleSlot slot, boolean selected) { this.slot = slot; this.selected = selected; }
        public ScheduleSlot getSlot() { return slot; }
        public boolean isSelected() { return selected; }
    }
}
