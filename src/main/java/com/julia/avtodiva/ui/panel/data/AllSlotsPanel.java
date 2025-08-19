package com.julia.avtodiva.ui.panel.data;

import com.julia.avtodiva.model.ScheduleSlot;
import com.julia.avtodiva.service.schedule.ScheduleSlotService;
import com.julia.avtodiva.ui.MainFrame;
import com.julia.avtodiva.ui.model.PanelName;
import com.julia.avtodiva.ui.panel.data.table.AllSlotsTableModel;
import com.julia.avtodiva.ui.panel.data.table.editor.DateComboBoxEditor;
import com.julia.avtodiva.ui.panel.data.table.editor.TimeComboBoxEditor;
import com.julia.avtodiva.ui.panel.data.table.renderer.LocalDateRenderer;
import com.julia.avtodiva.ui.state.AppState;
import org.springframework.context.annotation.Lazy;

import org.springframework.stereotype.Component;
import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
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

    private JPanel createBottomPanel(AllSlotsTableModel tableModel, JTable table) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton saveButton = new JButton("Зберегти вибране");
        saveButton.addActionListener(e -> {
            if (table.isEditing()) table.getCellEditor().stopCellEditing();

            List<ScheduleSlot> selectedSlots = tableModel.getSelectedSlots();
            if (selectedSlots.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Немає вибраних слотів", "Попередження", JOptionPane.WARNING_MESSAGE);
                return;
            }

            scheduleSlotService.saveAllSlots(selectedSlots);
            JOptionPane.showMessageDialog(this, "Слоти успішно добавлені", "Успіх", JOptionPane.INFORMATION_MESSAGE);

            List<ScheduleSlot> updatedSlots = scheduleSlotService.findAllSlots(
                    AppState.instructorName, AppState.carName, AppState.daysAhead
            );
            refreshAllSlots(updatedSlots);
        });

        JButton backButton = new JButton("Назад");
        backButton.addActionListener(l -> mainFrame.showPanel(PanelName.RANGE_SELECTION_PANEL.name()));

        panel.add(saveButton);
        panel.add(backButton);
        return panel;
    }
}

