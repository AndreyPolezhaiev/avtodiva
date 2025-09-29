package com.julia.avtodiva.ui.panel.data;

import com.julia.avtodiva.model.ScheduleSlot;
import com.julia.avtodiva.service.schedule.ScheduleSlotService;
import com.julia.avtodiva.ui.MainFrame;
import com.julia.avtodiva.ui.model.PanelName;
import com.julia.avtodiva.ui.panel.data.table.BookedSlotsTableModel;
import com.julia.avtodiva.ui.panel.data.table.editor.DateComboBoxEditor;
import com.julia.avtodiva.ui.panel.data.table.editor.TimeComboBoxEditor;
import com.julia.avtodiva.ui.panel.renderer.LocalDateRenderer;
import com.julia.avtodiva.ui.panel.dialog.SlotDetailsDialog;
import com.julia.avtodiva.ui.state.AppState;
import com.julia.avtodiva.ui.util.CarNameParser;
import com.julia.avtodiva.ui.util.CheckBoxComboBox;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Component
public class BookedSlotsPanel extends JPanel {
    private final MainFrame mainFrame;
    private final ScheduleSlotService scheduleSlotService;

    private final List<String> selectedTimes = new ArrayList<>();

    public BookedSlotsPanel(@Lazy MainFrame mainFrame, ScheduleSlotService scheduleSlotService) {
        this.mainFrame = mainFrame;
        this.scheduleSlotService = scheduleSlotService;
        setLayout(new BorderLayout());
    }

    public void refreshBookedSlots(List<ScheduleSlot> bookedSlots) {
        removeAll();

        BookedSlotsTableModel tableModel = new BookedSlotsTableModel(bookedSlots);
        JTable table = new JTable(tableModel);

        table.getTableHeader().addMouseListener(new MouseAdapter() {
            // Внутреннее состояние для отслеживания направления сортировки
            private int sortColumn = -1;
            private boolean isAscending = true;

            @Override
            public void mouseClicked(MouseEvent e) {
                int columnViewIndex = table.columnAtPoint(e.getPoint());
                int columnIndex = table.convertColumnIndexToModel(columnViewIndex);

                // Если кликнули по той же колонке, меняем направление
                if (columnIndex == sortColumn) {
                    isAscending = !isAscending;
                } else {
                    sortColumn = columnIndex;
                    isAscending = true; // Новая колонка, начинаем с возрастания
                }

                // Вызываем новый метод сортировки в модели!
                tableModel.sortByColumn(columnIndex, isAscending);

                // Теперь все индексы модели и представления снова совпадают
            }
        });

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

        add(buildTopPanel(tableModel), BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(createBottomPanel(tableModel, table), BorderLayout.SOUTH);

        revalidate();
        repaint();
    }

    private JPanel buildTopPanel(BookedSlotsTableModel tableModel) {
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JToggleButton selectAllSlots = selectAllSlotsButton("Вибрати всі", tableModel);

        CheckBoxComboBox timeCombo = new CheckBoxComboBox(AppState.DEFAULT_HOURS_STR, selectedTimes);

        JButton searchButton = getSearchButton();

        topPanel.add(selectAllSlots);
        topPanel.add(timeCombo);
        topPanel.add(searchButton);

        return topPanel;
    }

    private JButton getSearchButton() {
        JButton searchButton = new JButton("Пошук");
        searchButton.addActionListener(e -> {
            if (selectedTimes.isEmpty()) {
                refreshBookedSlots(scheduleSlotService.findBookedSlots(
                        AppState.instructorNames,
                        AppState.carNames,
                        AppState.startDate,
                        AppState.endDate
                ));
            } else {
                List<ScheduleSlot> freeSlots = scheduleSlotService.findBookedSlots(
                        AppState.instructorNames,
                        AppState.carNames,
                        AppState.startDate,
                        AppState.endDate
                );

                List<ScheduleSlot> filteredBookedSlots = scheduleSlotService.filterSlotsByTime(freeSlots, selectedTimes);
                refreshBookedSlots(filteredBookedSlots);
            }
        });
        return searchButton;
    }

    private JToggleButton selectAllSlotsButton(String name, BookedSlotsTableModel tableModel) {
        JToggleButton button = new JToggleButton(name);

        button.addActionListener(e -> {
            boolean selectAll = button.isSelected();
            tableModel.selectAll(selectAll);
        });

        return button;
    }

    private void addStudentClickListener(JTable table, BookedSlotsTableModel tableModel) {
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

    private JPanel createBottomPanel(BookedSlotsTableModel tableModel, JTable table) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton saveButton = new JButton("Зберегти вибране");
        saveButton.addActionListener(e -> {
            if (table.isEditing()) {
                table.getCellEditor().stopCellEditing();
            }

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
                            "Слот " + slot.getDate()
                                    + " " + slot.getTimeFrom()
                                    + " ("
                                    + slot.getInstructor().getName()
                                    + " або іншим інструктором"
                                    + ", на машині " + slot.getCar().getName()
                                    + ") вже зайнятий!",
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
            List<ScheduleSlot> updatedSlots = scheduleSlotService.findBookedSlots(
                    AppState.instructorNames,
                    AppState.carNames,
                    AppState.startDate,
                    AppState.endDate
            );
            refreshBookedSlots(updatedSlots);
        });

        JButton copyButton = getCopyButton(tableModel);
        JButton freeButton = getFreeButton(tableModel, table);
        JButton backButton = new JButton("Назад");
        backButton.addActionListener(l -> {
            mainFrame.showPanel(PanelName.RANGE_SELECTION_PANEL.name());
        });

        panel.add(copyButton);
        panel.add(saveButton);
        panel.add(freeButton);
        panel.add(backButton);
        return panel;
    }

    private JButton getCopyButton(BookedSlotsTableModel tableModel) {
        JButton copyButton = new JButton("Копіювати вибране");
        copyButton.addActionListener(e -> {
            List<ScheduleSlot> selectedSlots = tableModel.getSelectedSlots();
            if (selectedSlots.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Немає вибраних рядків", "Попередження", JOptionPane.WARNING_MESSAGE);
                return;
            }
            selectedSlots.sort(Comparator.comparing(ScheduleSlot::getDate)
                    .thenComparing(ScheduleSlot::getTimeFrom));

            StringBuilder sb = new StringBuilder();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, dd.MM.yyyy", new Locale("uk", "UA"));
            for (ScheduleSlot slot : selectedSlots) {
                String formattedDate = slot.getDate() != null
                        ? slot.getDate().format(formatter)
                        : "";

                sb.append(formattedDate).append("\t")
                        .append(slot.getInstructor().getName()).append("\t")
                        .append(CarNameParser.parseCarName(slot.getCar().getName())).append("\t")
                        .append(slot.getTimeFrom() != null ? slot.getTimeFrom() : "")
                        .append("\n");
            }

            StringSelection selection = new StringSelection(sb.toString());
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);

            JOptionPane.showMessageDialog(this, "Дані скопійовано у буфер обміну", "Інформація", JOptionPane.INFORMATION_MESSAGE);
        });

        return copyButton;
    }

    private JButton getFreeButton(BookedSlotsTableModel tableModel, JTable table) {
        JButton freeButton = new JButton("Звільнити місце");
        freeButton.addActionListener(e -> {
            if (table.isEditing()) table.getCellEditor().stopCellEditing();

            int[] selectedRows = table.getSelectedRows();
            if (selectedRows.length == 0) {
                JOptionPane.showMessageDialog(this, "Немає вибраних слотів", "Попередження", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int freed = 0;
            for (int row : selectedRows) {
                ScheduleSlot slot = tableModel.getSlotAt(row);
                if (slot.isBooked()) {
                    try {
                        // освобождаем слот
                        slot.setBooked(false);
                        slot.setStudent(null);
                        slot.setDescription(null);
                        slot.setLink(null);

                        scheduleSlotService.updateSlot(slot);
                        freed++;
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(this,
                                "Не вдалося звільнити слот " + slot.getDate() + " " + slot.getTimeFrom(),
                                "Помилка",
                                JOptionPane.WARNING_MESSAGE);
                    }
                }
            }

            if (freed > 0) {
                JOptionPane.showMessageDialog(this, "Звільнено слотів: " + freed, "Успіх", JOptionPane.INFORMATION_MESSAGE);
            }

            List<ScheduleSlot> updatedSlots = scheduleSlotService.findBookedSlots(
                    AppState.instructorNames,
                    AppState.carNames,
                    AppState.startDate,
                    AppState.endDate
            );
            refreshBookedSlots(updatedSlots);
        });
        return freeButton;
    }
}
