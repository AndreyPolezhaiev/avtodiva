package com.julia.avtodiva.ui.panel.data;

import com.julia.avtodiva.model.Instructor;
import com.julia.avtodiva.model.ScheduleSlot;
import com.julia.avtodiva.model.Weekend;
import com.julia.avtodiva.service.instructor.InstructorService;
import com.julia.avtodiva.service.schedule.ScheduleSlotService;
import com.julia.avtodiva.service.weekend.WeekendService;
import com.julia.avtodiva.ui.MainFrame;
import com.julia.avtodiva.ui.model.PanelName;
import com.julia.avtodiva.ui.panel.data.table.FreeSlotsTableModel;
import com.julia.avtodiva.ui.panel.data.table.InstructorWeekendsTableModel;
import com.julia.avtodiva.ui.panel.data.table.editor.DateComboBoxEditor;
import com.julia.avtodiva.ui.panel.data.table.editor.TimeComboBoxEditor;
import com.julia.avtodiva.ui.panel.renderer.LocalDateRenderer;
import com.julia.avtodiva.ui.panel.dialog.AddWeekendDialog;
import com.julia.avtodiva.ui.state.AppState;
import com.julia.avtodiva.ui.util.CheckBoxComboBox;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Component
public class InstructorWeekendsPanel extends JPanel {
    private final MainFrame mainFrame;
    private final InstructorService instructorService;
    private final WeekendService weekendService;
    private final ScheduleSlotService scheduleSlotService;

    public InstructorWeekendsPanel(@Lazy MainFrame mainFrame, InstructorService instructorService, WeekendService weekendService, ScheduleSlotService scheduleSlotService) {
        this.mainFrame = mainFrame;
        this.instructorService = instructorService;
        this.weekendService = weekendService;
        this.scheduleSlotService = scheduleSlotService;
        setLayout(new BorderLayout());
    }

    public void refreshInstructor(Instructor instructor) {
        removeAll();

        final Long instructorId = instructor.getId();
        InstructorWeekendsTableModel tableModel = new InstructorWeekendsTableModel(instructor);
        JTable table = new JTable(tableModel);
        table.setRowHeight(AppState.COLUMN_HEIGHT);

        int[][] defaultHours = AppState.DEFAULT_HOURS;
        TimeComboBoxEditor timeEditor = new TimeComboBoxEditor(defaultHours);
        table.getColumnModel().getColumn(3).setCellEditor(timeEditor);
        table.getColumnModel().getColumn(4).setCellEditor(timeEditor);

        DateComboBoxEditor dateEditor = new DateComboBoxEditor();
        table.getColumnModel().getColumn(2).setCellEditor(dateEditor);

        LocalDateRenderer dateRenderer = new LocalDateRenderer();
        table.getColumnModel().getColumn(2).setCellRenderer(dateRenderer);

        table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollPane = new JScrollPane(table);

        add(buildTopPanel(tableModel), BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(createBottomPanel(tableModel, table, instructorId), BorderLayout.SOUTH);

        revalidate();
        repaint();
    }

    private JPanel buildTopPanel(InstructorWeekendsTableModel tableModel) {
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JToggleButton selectAllSlots = selectAllSlotsButton("Вибрати всі", tableModel);

        topPanel.add(selectAllSlots);

        return topPanel;
    }

    private JPanel createBottomPanel(InstructorWeekendsTableModel tableModel, JTable table, Long instructorId) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton copyButton = getCopyButton(tableModel);

        JButton addButton = getAddButton(table, instructorId);

        JButton deleteButton = getDeleteButton(tableModel, table, instructorId);

        JButton saveButton = getSaveButton(tableModel, table, instructorId);

        JButton backButton = new JButton("Назад");
        backButton.addActionListener(l -> mainFrame.showPanel(PanelName.RANGE_SELECTION_PANEL.name()));

        panel.add(copyButton);
        panel.add(addButton);
        panel.add(deleteButton);
        panel.add(saveButton);
        panel.add(backButton);
        return panel;
    }

    private JButton getSaveButton(InstructorWeekendsTableModel tableModel, JTable table, Long instructorId) {
        JButton saveButton = new JButton("Зберегти вибране");
        saveButton.addActionListener(e -> {
            if (table.isEditing()) table.getCellEditor().stopCellEditing();
            List<Weekend> selected = tableModel.getSelectedWeekends();
            if (selected.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Немає вибраних вихідних", "Попередження", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Instructor inst = instructorService.findById(instructorId);
            List<com.julia.avtodiva.model.ScheduleSlot> bookedSlots =
                    scheduleSlotService.findAllBookedSlotsByInstructorName(inst.getName());

            StringBuilder conflicts = new StringBuilder();

            for (Weekend w : selected) {
                boolean conflict = bookedSlots.stream().anyMatch(slot ->
                        slot.getDate().equals(w.getDay()) &&
                                !(slot.getTimeTo().isBefore(w.getTimeFrom()) || slot.getTimeFrom().isAfter(w.getTimeTo()))
                );
                if (conflict) {
                    conflicts.append(w.getDay()).append(" має зайняті слоти!\n");
                }
            }

            if (!conflicts.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Не вдалося зберегти вихідні:\n" + conflicts,
                        "Конфлікт", JOptionPane.WARNING_MESSAGE);
                refreshInstructor(inst);
                return;
            }

            weekendService.saveAllWeekends(selected);
            JOptionPane.showMessageDialog(this, "Вихідні успішно збережені", "Успіх", JOptionPane.INFORMATION_MESSAGE);
            Instructor fresh = instructorService.findById(instructorId);
            refreshInstructor(fresh);
        });
        return saveButton;
    }

    private JButton getDeleteButton(InstructorWeekendsTableModel tableModel, JTable table, Long instructorId) {
        JButton deleteButton = new JButton("Видалити вибраний вихідний");
        deleteButton.addActionListener(e -> {
            if (table.isEditing()) table.getCellEditor().stopCellEditing();
            List<Weekend> selected = tableModel.getSelectedWeekends();
            if (selected.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Немає вибраних вихідних", "Попередження", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String msg = selected.size() == 1
                    ? "Видалити вибраний вихідний?"
                    : "Видалити вибраний вихідний (" + selected.size() + " шт.)?";
            int ans = JOptionPane.showConfirmDialog(this, msg, "Підтвердження", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (ans == JOptionPane.YES_OPTION) {
                weekendService.deleteAllWeekends(selected); // реализуй в сервисе удаление списка
                JOptionPane.showMessageDialog(this, "Видалення виконано", "Готово", JOptionPane.INFORMATION_MESSAGE);
                Instructor fresh = instructorService.findById(instructorId);
                refreshInstructor(fresh);
            }
        });
        return deleteButton;
    }

    private JButton getAddButton(JTable table, Long instructorId) {
        JButton addButton = new JButton("Додати вихідний");
        addButton.addActionListener(e -> {
            if (table.isEditing()) table.getCellEditor().stopCellEditing();

            Window parent = SwingUtilities.getWindowAncestor(this);
            AddWeekendDialog dlg = new AddWeekendDialog(
                    parent,
                    instructorId,
                    instructorService,
                    weekendService,
                    scheduleSlotService
            );
            dlg.setVisible(true);

            Instructor fresh = instructorService.findById(instructorId);
            refreshInstructor(fresh);
        });
        return addButton;
    }

    private JToggleButton selectAllSlotsButton(String name,InstructorWeekendsTableModel tableModel) {
        JToggleButton button = new JToggleButton(name);

        button.addActionListener(e -> {
            boolean selectAll = button.isSelected();
            tableModel.selectAll(selectAll);
        });

        return button;
    }

    private JButton getCopyButton(InstructorWeekendsTableModel tableModel) {
        JButton copyButton = new JButton("Копіювати вибране");
        copyButton.addActionListener(e -> {
            List<Weekend> selectedWeekends = tableModel.getSelectedWeekends();
            if (selectedWeekends.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Немає вибраних рядків", "Попередження", JOptionPane.WARNING_MESSAGE);
                return;
            }
            selectedWeekends.sort(Comparator.comparing(Weekend::getDay)
                    .thenComparing(Weekend::getTimeFrom));

            StringBuilder sb = new StringBuilder();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, dd.MM.yyyy", new Locale("uk", "UA"));
            for (Weekend weekend : selectedWeekends) {
                String formattedDate = weekend.getDay() != null
                        ? weekend.getDay().format(formatter)
                        : "";

                sb.append(formattedDate).append("\t")
                        .append(weekend.getInstructor().getName()).append("\t")
                        .append(weekend.getTimeFrom() != null ? weekend.getTimeFrom() : "").append("-")
                        .append(weekend.getTimeTo() != null ? weekend.getTimeTo() : "")
                        .append("\n");
            }

            StringSelection selection = new StringSelection(sb.toString());
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);

            JOptionPane.showMessageDialog(this, "Дані скопійовано у буфер обміну", "Інформація", JOptionPane.INFORMATION_MESSAGE);
        });

        return copyButton;
    }
}
