package com.julia.avtodiva.ui.panel.data;

import com.julia.avtodiva.model.ScheduleSlot;
import com.julia.avtodiva.service.schedule.ScheduleSlotService;
import com.julia.avtodiva.ui.MainFrame;
import com.julia.avtodiva.ui.model.PanelName;
import com.julia.avtodiva.ui.panel.data.table.AllSlotsTableModel;
import com.julia.avtodiva.ui.panel.data.table.editor.MultiLineCellEditor;
import com.julia.avtodiva.ui.panel.data.table.renderer.MultiLineCellRenderer;
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
        JTable table = createTable(tableModel);

        table.getColumnModel().getColumn(7).setCellRenderer(new MultiLineCellRenderer());
        table.getColumnModel().getColumn(7).setCellEditor(new MultiLineCellEditor());
        table.getColumnModel().getColumn(8).setCellRenderer(new MultiLineCellRenderer());
        table.getColumnModel().getColumn(8).setCellEditor(new MultiLineCellEditor());

        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);
        add(createBottomPanel(tableModel, table), BorderLayout.SOUTH);

        // базовая (дефолтная) высота строки — видна обоим слушателям
        final int baseRowHeight = table.getRowHeight();

        table.addMouseListener(new java.awt.event.MouseAdapter() {
            private int expandedRow = -1;

            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int viewRow = table.rowAtPoint(e.getPoint());
                if (viewRow < 0) return;

                if (viewRow == expandedRow) {
                    table.setRowHeight(viewRow, baseRowHeight);
                    expandedRow = -1;
                } else {
                    if (expandedRow != -1) table.setRowHeight(expandedRow, baseRowHeight);
                    int h = computePreferredRowHeight(table, viewRow, new int[]{7, 8});
                    table.setRowHeight(viewRow, Math.max(h, baseRowHeight));
                    expandedRow = viewRow;
                }
            }
        });

        table.getColumnModel().addColumnModelListener(new javax.swing.event.TableColumnModelListener() {
            @Override public void columnMarginChanged(javax.swing.event.ChangeEvent e) {
                // пересчитать высоту раскрытой строки при изменении ширины колонок
                for (int r = 0; r < table.getRowCount(); r++) {
                    int rh = table.getRowHeight(r);
                    if (rh > baseRowHeight) {
                        int newH = computePreferredRowHeight(table, r, new int[]{7, 8});
                        table.setRowHeight(r, Math.max(newH, baseRowHeight));
                        break;
                    }
                }
            }
            @Override public void columnAdded(javax.swing.event.TableColumnModelEvent e) {}
            @Override public void columnRemoved(javax.swing.event.TableColumnModelEvent e) {}
            @Override public void columnMoved(javax.swing.event.TableColumnModelEvent e) {}
            @Override public void columnSelectionChanged(javax.swing.event.ListSelectionEvent e) {}
        });

        revalidate();
        repaint();
    }

    // Универсальный пересчёт требуемой высоты строки по набору колонок (wrap-aware)
    // важно: java.awt.Component, иначе конфликт со Spring @Component
    private static int computePreferredRowHeight(JTable table, int row, int[] columns) {
        int max = table.getRowHeight();
        for (int col : columns) {
            if (col < 0 || col >= table.getColumnCount()) continue;
            TableCellRenderer renderer = table.getCellRenderer(row, col);
            java.awt.Component comp = table.prepareRenderer(renderer, row, col);
            int colWidth = table.getColumnModel().getColumn(col).getWidth();
            comp.setSize(colWidth, Integer.MAX_VALUE);        // подсказали реальную ширину
            java.awt.Dimension pref = comp.getPreferredSize(); // и получили корректную высоту
            max = Math.max(max, pref.height + table.getRowMargin());
        }
        return max + 2;
    }



    private JTable createTable(AllSlotsTableModel model) {
        JTable table = new JTable(model);
        table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        return table;
    }

    private JPanel createBottomPanel(AllSlotsTableModel tableModel, JTable table) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton saveButton = new JButton("Сохранить выбранные");
        saveButton.addActionListener(e -> {
            if (table.isEditing()) table.getCellEditor().stopCellEditing();

            List<ScheduleSlot> selectedSlots = tableModel.getSelectedSlots();
            if (selectedSlots.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Нет выбранных слотов", "Предупреждение", JOptionPane.WARNING_MESSAGE);
                return;
            }

            scheduleSlotService.saveAllSlots(selectedSlots);
            JOptionPane.showMessageDialog(this, "Слоты успешно сохранены", "Успех", JOptionPane.INFORMATION_MESSAGE);

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

