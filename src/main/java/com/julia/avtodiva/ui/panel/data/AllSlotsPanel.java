package com.julia.avtodiva.ui.panel.data;

import com.julia.avtodiva.model.ScheduleSlot;
import com.julia.avtodiva.model.Student;
import com.julia.avtodiva.service.schedule.ScheduleSlotService;
import com.julia.avtodiva.ui.MainFrame;
import com.julia.avtodiva.ui.model.PanelName;
import com.julia.avtodiva.ui.panel.data.table.AllSlotsTableModel;
import com.julia.avtodiva.ui.panel.data.table.editor.DateComboBoxEditor;
import com.julia.avtodiva.ui.panel.data.table.editor.TimeComboBoxEditor;
import com.julia.avtodiva.ui.panel.data.table.renderer.LocalDateRenderer;
import com.julia.avtodiva.ui.panel.dialog.SlotDetailsDialog;
import com.julia.avtodiva.ui.state.AppState;
import org.springframework.context.annotation.Lazy;

import org.springframework.stereotype.Component;
import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

@Component
public class AllSlotsPanel extends JPanel {
    private final MainFrame mainFrame;
    private final ScheduleSlotService scheduleSlotService;

    public AllSlotsPanel(@Lazy MainFrame mainFrame, ScheduleSlotService scheduleSlotService) {
        this.mainFrame = mainFrame;
        this.scheduleSlotService = scheduleSlotService;
        setLayout(new BorderLayout());
    }

    public void refreshAllSlots(List<ScheduleSlot> allSlots) {
        removeAll();

        AllSlotsTableModel tableModel = new AllSlotsTableModel(allSlots);
        JTable table = new JTable(tableModel);
        addStudentClickListener(table, tableModel);
        table.setRowHeight(AppState.COLUMN_HEIGHT);

        int[][] defaultHours = AppState.DEFAULT_HOURS;
        TimeComboBoxEditor timeEditor = new TimeComboBoxEditor(defaultHours);
        table.getColumnModel().getColumn(4).setCellEditor(timeEditor);
        table.getColumnModel().getColumn(5).setCellEditor(timeEditor);

        DateComboBoxEditor dateEditor = new DateComboBoxEditor();
        table.getColumnModel().getColumn(1).setCellEditor(dateEditor);

        LocalDateRenderer dateRenderer = new LocalDateRenderer();
        table.getColumnModel().getColumn(1).setCellRenderer(dateRenderer);

        JScrollPane scrollPane = new JScrollPane(table);

        add(scrollPane, BorderLayout.CENTER);
        add(createBottomPanel(tableModel, table), BorderLayout.SOUTH);

        revalidate();
        repaint();
    }

    private void addStudentClickListener(JTable table, AllSlotsTableModel tableModel) {
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                int col = table.columnAtPoint(e.getPoint());

                if (col >= 6 && row >= 0) { // колонка "Учениця"
                    boolean isSelected = (Boolean) tableModel.getValueAt(row, 0);
                    ScheduleSlot slot = tableModel.getSlotAt(row);

                    new SlotDetailsDialog(
                            SwingUtilities.getWindowAncestor(table),
                            tableModel,
                            slot,
                            row,
                            isSelected
                    ).setVisible(true);
                }
            }
        });
    }

    private JPanel createBottomPanel(AllSlotsTableModel tableModel, JTable table) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton saveButton = getSaveButton(tableModel, table);

        JButton copyButton = getCopyButton(tableModel);

        JButton backButton = new JButton("Назад");
        backButton.addActionListener(
                l -> mainFrame.showPanel(PanelName.RANGE_SELECTION_PANEL.name())
        );

        panel.add(copyButton);
        panel.add(saveButton);
        panel.add(backButton);
        return panel;
    }

    private JButton getSaveButton(AllSlotsTableModel tableModel, JTable table) {
        JButton saveButton = new JButton("Зберегти вибране");
        saveButton.addActionListener(e -> {
            if (table.isEditing()) table.getCellEditor().stopCellEditing();

            List<ScheduleSlot> selectedSlots = tableModel.getSelectedSlots();
            if (selectedSlots.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Немає вибраних слотів", "Попередження", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int saved = 0;
            int failed = 0;

            for (ScheduleSlot slot : selectedSlots) {
                if (slot.getStudent() == null || slot.getStudent().getName() == null || slot.getStudent().getName().isBlank()) {
                    JOptionPane.showMessageDialog(this,
                            "Не вказано ім'я студента для слота "
                                    + slot.getDate() + " " + slot.getTimeFrom(),
                            "Помилка", JOptionPane.WARNING_MESSAGE);
                    failed++;
                    continue; // пропускаем сохранение этого слота
                }

                try {
                    // пробуем применить изменения через сервис
                    boolean ok = scheduleSlotService.rescheduleSlot(slot);
                    if (ok) {
                        saved++;
                    } else {
                        failed++;
                    }
                } catch (Exception ex) {
                    failed++;
                    JOptionPane.showMessageDialog(this,
                            "Слот " + slot.getDate() + " " + slot.getTimeFrom() +
                                    " (" + slot.getInstructor().getName() + ", " + slot.getCar().getName() + ") вже зайнятий!",
                            "Помилка",
                            JOptionPane.WARNING_MESSAGE);
                }
            }

            if (saved > 0) {
                JOptionPane.showMessageDialog(this, "Успішно збережено: " + saved + " слотів", "Успіх", JOptionPane.INFORMATION_MESSAGE);
            }

            if (failed > 0) {
                JOptionPane.showMessageDialog(this, "Не вдалося зберегти: " + failed + " слотів", "Помилка", JOptionPane.WARNING_MESSAGE);
            }

            List<ScheduleSlot> updatedSlots = scheduleSlotService.findAllSlots(
                    AppState.instructorNames, AppState.carNames, AppState.daysAhead
            );
            refreshAllSlots(updatedSlots);
        });
        return saveButton;
    }

    private JButton getCopyButton(AllSlotsTableModel tableModel) {
        JButton copyButton = new JButton("Копіювати вибране");
        copyButton.addActionListener(e -> {
            java.util.List<ScheduleSlot> selectedSlots = tableModel.getSelectedSlots();
            if (selectedSlots.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Немає вибраних рядків", "Попередження", JOptionPane.WARNING_MESSAGE);
                return;
            }

            StringBuilder sb = new StringBuilder();
            for (ScheduleSlot slot : selectedSlots) {
                // колонки 1,2,3,4 → дата, інструктор, машина, час з
                sb.append(slot.getDate()).append("\t")
                        .append(slot.getInstructor().getName()).append("\t")
                        .append(slot.getCar().getName()).append("\t")
                        .append(slot.getTimeFrom() != null ? slot.getTimeFrom() : "")
                        .append("\n");
            }

            StringSelection selection = new StringSelection(sb.toString());
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);

            JOptionPane.showMessageDialog(this, "Дані скопійовано у буфер обміну", "Інформація", JOptionPane.INFORMATION_MESSAGE);
        });

        return copyButton;
    }
}

