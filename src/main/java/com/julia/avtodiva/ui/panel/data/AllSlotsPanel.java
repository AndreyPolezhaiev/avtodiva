package com.julia.avtodiva.ui.panel.data;

import com.julia.avtodiva.model.ScheduleSlot;
import com.julia.avtodiva.service.schedule.ScheduleSlotService;
import com.julia.avtodiva.ui.MainFrame;
import com.julia.avtodiva.ui.model.PanelName;
import com.julia.avtodiva.ui.panel.data.table.AllSlotsTableModel;
import com.julia.avtodiva.ui.panel.data.table.editor.DateComboBoxEditor;
import com.julia.avtodiva.ui.panel.data.table.editor.TimeComboBoxEditor;
import com.julia.avtodiva.ui.panel.renderer.LocalDateRenderer;
import com.julia.avtodiva.ui.panel.dialog.SlotDetailsDialog;
import com.julia.avtodiva.ui.state.AppState;
import com.julia.avtodiva.ui.util.CheckBoxComboBox;
import org.springframework.context.annotation.Lazy;

import org.springframework.stereotype.Component;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

@Component
public class AllSlotsPanel extends JPanel {
    private final MainFrame mainFrame;
    private final ScheduleSlotService scheduleSlotService;

    private JTextField searchField;
    private JLabel searchLabel;
    private TableRowSorter<AllSlotsTableModel> sorter;

    private final List<String> selectedTimes = new ArrayList<>();

    public AllSlotsPanel(@Lazy MainFrame mainFrame, ScheduleSlotService scheduleSlotService) {
        this.mainFrame = mainFrame;
        this.scheduleSlotService = scheduleSlotService;
        setLayout(new BorderLayout());
    }

    public void refreshAllSlots(List<ScheduleSlot> allSlots) {
        removeAll();

        AllSlotsTableModel tableModel = new AllSlotsTableModel(allSlots);
        JTable table = new JTable(tableModel);

        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);

        setupSearchFunctionality(table);

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

    private JPanel buildTopPanel(AllSlotsTableModel tableModel) {
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JToggleButton selectAllSlots = selectAllSlotsButton("Вибрати всі", tableModel);

        CheckBoxComboBox timeCombo = new CheckBoxComboBox(AppState.DEFAULT_HOURS_STR, selectedTimes);

        JButton searchButton = getSearchButton();

        topPanel.add(selectAllSlots);
        topPanel.add(timeCombo);
        topPanel.add(searchButton);

        if (searchField != null) {
            topPanel.add(searchLabel);
            topPanel.add(searchField);
        }

        return topPanel;
    }

    private void setupSearchFunctionality(JTable table) {
        if (searchField == null) {
            searchField = new JTextField(15);
            searchLabel = new JLabel("Пошук (Ctrl+F):");
            searchField.setVisible(false);
            searchLabel.setVisible(false);
        }

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            private void applyFilter() {
                String text = searchField.getText();
                if (text.isEmpty()) {
                    sorter.setRowFilter(null);
                } else {
                    try {
                        // Регулярное выражение для поиска подстроки без учета регистра
                        String regex = "(?i).*" + Pattern.quote(text) + ".*";
                        sorter.setRowFilter(RowFilter.regexFilter(regex));
                    } catch (PatternSyntaxException e) {
                        sorter.setRowFilter(null);
                    }
                }
            }

            @Override public void insertUpdate(DocumentEvent e) { applyFilter(); }
            @Override public void removeUpdate(DocumentEvent e) { applyFilter(); }
            @Override public void changedUpdate(DocumentEvent e) { applyFilter(); }
        });

        InputMap im = table.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = table.getActionMap();

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_F, KeyEvent.CTRL_DOWN_MASK), "startSearch");

        am.put("startSearch", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                searchLabel.setVisible(true);
                searchField.setVisible(true);

                AllSlotsPanel.this.revalidate();
                AllSlotsPanel.this.repaint();

                searchField.requestFocusInWindow();
                searchField.selectAll();
            }
        });
    }

    private JButton getSearchButton() {
        JButton searchButton = new JButton("Пошук");
        searchButton.addActionListener(e -> {
            if (selectedTimes.isEmpty()) {
                refreshAllSlots(scheduleSlotService.findAllSlots(
                        AppState.instructorNames,
                        AppState.carNames,
                        AppState.startDate,
                        AppState.endDate
                ));
            } else {
                List<ScheduleSlot> allSlots = scheduleSlotService.findAllSlots(
                        AppState.instructorNames,
                        AppState.carNames,
                        AppState.startDate,
                        AppState.endDate
                );

                List<ScheduleSlot> filteredAllSlots = scheduleSlotService.filterSlotsByTime(allSlots, selectedTimes);
                refreshAllSlots(filteredAllSlots);
            }
        });
        return searchButton;
    }

    private JToggleButton selectAllSlotsButton(String name, AllSlotsTableModel tableModel) {
        JToggleButton button = new JToggleButton(name);

        button.addActionListener(e -> {
            boolean selectAll = button.isSelected();
            tableModel.selectAll(selectAll);
        });

        return button;
    }

    private void addStudentClickListener(JTable table, AllSlotsTableModel tableModel) {
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                int col = table.columnAtPoint(e.getPoint());

                if (col >= 6 && row >= 0) { // колонка "Учениця"
                    int modelRow = table.convertRowIndexToModel(row);
                    boolean isSelected = (Boolean) tableModel.getValueAt(modelRow, 0);
                    ScheduleSlot slot = tableModel.getSlotAt(modelRow);

                    new SlotDetailsDialog(
                            SwingUtilities.getWindowAncestor(table),
                            tableModel,
                            slot,
                            modelRow,
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
        JButton freeButton = getFreeButton(tableModel, table);

        JButton backButton = new JButton("Назад");
        backButton.addActionListener(
                l -> mainFrame.showPanel(PanelName.RANGE_SELECTION_PANEL.name())
        );

        panel.add(copyButton);
        panel.add(saveButton);
        panel.add(freeButton);
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
                            "Слот " + slot.getDate()
                                    + " " + slot.getTimeFrom()
                                    + " ("
                                    + slot.getInstructor().getName()
                                    + " або іншим інструктором"
                                    + ", на машині " + slot.getCar().getName()
                                    + ") вже зайнятий! "
                                    + " Або слот перепадає на вихідний",
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
                    AppState.instructorNames,
                    AppState.carNames,
                    AppState.startDate,
                    AppState.endDate
            );
            refreshAllSlots(updatedSlots);
        });
        return saveButton;
    }

    private JButton getCopyButton(AllSlotsTableModel tableModel) {
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

    private JButton getFreeButton(AllSlotsTableModel tableModel, JTable table) {
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
                int modelRow = table.convertRowIndexToModel(row);
                ScheduleSlot slot = tableModel.getSlotAt(modelRow);
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

            List<ScheduleSlot> updatedSlots = scheduleSlotService.findAllSlots(
                    AppState.instructorNames,
                    AppState.carNames,
                    AppState.startDate,
                    AppState.endDate
            );
            refreshAllSlots(updatedSlots);
        });
        return freeButton;
    }

}

