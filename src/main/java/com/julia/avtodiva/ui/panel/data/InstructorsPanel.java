// src/main/java/com/julia/avtodiva/ui/panel/data/InstructorsPanel.java
package com.julia.avtodiva.ui.panel.data;

import com.julia.avtodiva.model.Instructor;
import com.julia.avtodiva.model.Weekend;
import com.julia.avtodiva.service.instructor.InstructorService;
import com.julia.avtodiva.service.weekend.WeekendService;
import com.julia.avtodiva.ui.MainFrame;
import com.julia.avtodiva.ui.model.PanelName;
import com.julia.avtodiva.ui.panel.data.table.InstructorWeekendsTableModel;
import com.julia.avtodiva.ui.panel.data.table.editor.TimeComboBoxEditor;
import com.julia.avtodiva.ui.panel.dialog.AddWeekendDialog;
import com.julia.avtodiva.ui.state.AppState;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.*;
import java.util.List;

@Component
public class InstructorsPanel extends JPanel {
    private final MainFrame mainFrame;
    private final InstructorService instructorService;
    private final WeekendService weekendService;

    public InstructorsPanel(@Lazy MainFrame mainFrame, InstructorService instructorService, WeekendService weekendService) {
        this.mainFrame = mainFrame;
        this.instructorService = instructorService;
        this.weekendService = weekendService;
        setLayout(new BorderLayout());
    }

    public void refreshInstructor(Instructor instructor) {
        removeAll();

        final Long instructorId = instructor.getId();
        InstructorWeekendsTableModel tableModel = new InstructorWeekendsTableModel(instructor);
        JTable table = new JTable(tableModel);

        int[][] defaultHours = AppState.DEFAULT_HOURS;
        TimeComboBoxEditor timeEditor = new TimeComboBoxEditor(defaultHours);
        table.getColumnModel().getColumn(3).setCellEditor(timeEditor);
        table.getColumnModel().getColumn(4).setCellEditor(timeEditor);

        table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);
        add(createBottomPanel(tableModel, table, instructorId), BorderLayout.SOUTH);

        revalidate();
        repaint();
    }

    private JPanel createBottomPanel(InstructorWeekendsTableModel tableModel, JTable table, Long instructorId) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton addButton = getAddButton(table, instructorId);

        JButton deleteButton = getDeleteButton(tableModel, table, instructorId);

        JButton saveButton = getSaveButton(tableModel, table, instructorId);

        JButton backButton = new JButton("Назад");
        backButton.addActionListener(l -> mainFrame.showPanel(PanelName.RANGE_SELECTION_PANEL.name()));

        panel.add(addButton);
        panel.add(deleteButton);
        panel.add(saveButton);
        panel.add(backButton);
        return panel;
    }

    private JButton getSaveButton(InstructorWeekendsTableModel tableModel, JTable table, Long instructorId) {
        JButton saveButton = new JButton("Сохранить выбранные");
        saveButton.addActionListener(e -> {
            if (table.isEditing()) table.getCellEditor().stopCellEditing();
            List<Weekend> selected = tableModel.getSelectedWeekends();
            if (selected.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Нет выбранных выходных", "Предупреждение", JOptionPane.WARNING_MESSAGE);
                return;
            }
            weekendService.saveAllWeekends(selected);
            JOptionPane.showMessageDialog(this, "Выходные успешно сохранены", "Успех", JOptionPane.INFORMATION_MESSAGE);
            Instructor fresh = instructorService.findById(instructorId);
            refreshInstructor(fresh);
        });
        return saveButton;
    }

    private JButton getDeleteButton(InstructorWeekendsTableModel tableModel, JTable table, Long instructorId) {
        JButton deleteButton = new JButton("Удалить выбранный выходной");
        deleteButton.addActionListener(e -> {
            if (table.isEditing()) table.getCellEditor().stopCellEditing();
            List<Weekend> selected = tableModel.getSelectedWeekends();
            if (selected.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Нет выбранных выходных", "Предупреждение", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String msg = selected.size() == 1
                    ? "Удалить выбранный выходной?"
                    : "Удалить выбранные выходные (" + selected.size() + " шт.)?";
            int ans = JOptionPane.showConfirmDialog(this, msg, "Подтверждение", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (ans == JOptionPane.YES_OPTION) {
                weekendService.deleteAllWeekends(selected); // реализуй в сервисе удаление списка
                JOptionPane.showMessageDialog(this, "Удаление выполнено", "Готово", JOptionPane.INFORMATION_MESSAGE);
                Instructor fresh = instructorService.findById(instructorId);
                refreshInstructor(fresh);
            }
        });
        return deleteButton;
    }

    private JButton getAddButton(JTable table, Long instructorId) {
        JButton addButton = new JButton("Добавить выходной");
        addButton.addActionListener(e -> {
            if (table.isEditing()) table.getCellEditor().stopCellEditing();

            Window parent = SwingUtilities.getWindowAncestor(this);
            AddWeekendDialog dlg = new AddWeekendDialog(
                    parent,
                    instructorId,
                    instructorService,
                    weekendService
            );
            dlg.setVisible(true);

            Instructor fresh = instructorService.findById(instructorId);
            refreshInstructor(fresh);
        });
        return addButton;
    }
}
