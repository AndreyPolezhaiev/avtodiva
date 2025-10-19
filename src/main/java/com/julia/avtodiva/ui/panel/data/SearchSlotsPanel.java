package com.julia.avtodiva.ui.panel.data;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.matchers.TextMatcherEditor;
import ca.odell.glazedlists.swing.AutoCompleteSupport;
import ca.odell.glazedlists.swing.EventComboBoxModel;
import com.julia.avtodiva.model.ScheduleSlot;
import com.julia.avtodiva.service.car.CarService;
import com.julia.avtodiva.service.instructor.InstructorService;
import com.julia.avtodiva.service.schedule.ScheduleSlotService;
import com.julia.avtodiva.service.student.StudentService;
import com.julia.avtodiva.ui.MainFrame;
import com.julia.avtodiva.ui.model.PanelName;
import com.julia.avtodiva.ui.panel.data.table.InstructorWeekendsTableModel;
import com.julia.avtodiva.ui.panel.data.table.SearchSlotsTableModel;
import com.julia.avtodiva.ui.panel.data.table.editor.DateComboBoxEditor;
import com.julia.avtodiva.ui.panel.data.table.editor.TimeComboBoxEditor;
import com.julia.avtodiva.ui.panel.renderer.LocalDateRenderer;
import com.julia.avtodiva.ui.panel.dialog.SlotDetailsDialog;
import com.julia.avtodiva.ui.state.AppState;
import com.julia.avtodiva.ui.util.SingleSlotButton;
import org.jdesktop.swingx.JXComboBox;
import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

@Component
public class SearchSlotsPanel extends JPanel {
    private final MainFrame mainFrame;
    private final ScheduleSlotService scheduleSlotService;
    private final StudentService studentService;
    private final InstructorService instructorService;
    private final CarService carService;
    private JComboBox<String> instructorCombo;
    private JComboBox<String> studentCombo;
    private final EventList<String> studentsEventList = new BasicEventList<>();

    // Таблица соответствий русские ↔ латинские
    private static final Map<Character, Character> SIMILAR_MAP = Map.ofEntries(
            Map.entry('а', 'a'), Map.entry('А', 'A'),
            Map.entry('о', 'o'), Map.entry('О', 'O'),
            Map.entry('е', 'e'), Map.entry('Е', 'E'),
            Map.entry('р', 'p'), Map.entry('Р', 'P'),
            Map.entry('с', 'c'), Map.entry('С', 'C'),
            Map.entry('х', 'x'), Map.entry('Х', 'X'),
            Map.entry('у', 'y'), Map.entry('У', 'Y'),
            Map.entry('к', 'k'), Map.entry('К', 'K'),
            Map.entry('м', 'm'), Map.entry('М', 'M'),
            Map.entry('н', 'h'), Map.entry('Н', 'H'),
            Map.entry('в', 'b'), Map.entry('В', 'B'),
            Map.entry('т', 't'), Map.entry('Т', 'T'),
            Map.entry('і', 'i'), Map.entry('І', 'I')
    );

    /**
     * TextFilterator для поддержки поиска как на русском, так и на латинице.
     * Транслитерирует имя, используя SIMILAR_MAP.
     */
    private static class CyrillicTextFilterator implements TextFilterator<String> {
        @Override
        public void getFilterStrings(List<String> baseList, String studentName) {
            // 1. Добавляем исходное имя (для обычного поиска)
            baseList.add(studentName);

            // 2. Добавляем транслитерированное имя
            StringBuilder sb = new StringBuilder();
            for (char c : studentName.toCharArray()) {
                // Заменяем букву, если найдено совпадение в карте
                sb.append(SIMILAR_MAP.getOrDefault(c, c));
            }
            baseList.add(sb.toString());
        }
    }

    private enum SearchMode {
        INSTRUCTOR,
        STUDENT,
        BOTH,
        NONE
    }

    private SearchMode lastSearchMode = SearchMode.NONE;

    public SearchSlotsPanel(@Lazy MainFrame mainFrame, ScheduleSlotService scheduleSlotService, StudentService studentService, InstructorService instructorService, CarService carService) {
        this.mainFrame = mainFrame;
        this.scheduleSlotService = scheduleSlotService;
        this.studentService = studentService;
        this.instructorService = instructorService;
        this.carService = carService;
        setLayout(new BorderLayout());
    }

    public void refreshSearchSlots() {
        removeAll();

        updateCombos();
        
        SearchSlotsTableModel tableModel = new SearchSlotsTableModel(List.of());
        JTable table = new JTable(tableModel);
        // sorting listener
        addSortableHeader(table, tableModel);

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

    private void addSortableHeader(JTable table, SearchSlotsTableModel tableModel) {
        table.getTableHeader().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int columnIndex = table.getTableHeader().columnAtPoint(e.getPoint());
                if (columnIndex != -1) {
                    tableModel.sortByColumn(columnIndex);
                }
            }
        });
    }

    private void updateCombos() {
        String selectedInstructor = instructorCombo != null ? (String) instructorCombo.getSelectedItem() : null;
        String selectedStudent = studentCombo != null ? (String) studentCombo.getSelectedItem() : null;

        String[] instructorsNames = instructorService.getInstructorsNames();
        String[] studentsNames = studentService.getStudentsNames();

        if (instructorCombo == null) {
            instructorCombo = new JComboBox<>(instructorsNames);
        } else {
            instructorCombo.setModel(new DefaultComboBoxModel<>(instructorsNames));
            if (selectedInstructor != null && List.of(instructorsNames).contains(selectedInstructor)) {
                instructorCombo.setSelectedItem(selectedInstructor);
            }
        }

        if (studentCombo == null) {
            studentsEventList.addAll(Arrays.asList(studentsNames));
            EventComboBoxModel<String> studentModel = new EventComboBoxModel<>(studentsEventList);

            studentCombo = new JXComboBox(studentModel);
            studentCombo.setEditable(true);
            studentCombo.setMaximumRowCount(30);

            if (studentsNames.length > 0) {
                String longestName = Arrays.stream(studentsNames)
                        .max(Comparator.comparingInt(String::length))
                        .orElse(studentsNames[0]);
                studentCombo.setPrototypeDisplayValue(longestName);
            }

            AutoCompleteSupport<String> support = AutoCompleteSupport.install(
                    studentCombo, studentsEventList, new CyrillicTextFilterator()
            );

            support.setFilterMode(TextMatcherEditor.CONTAINS);

        } else {
            studentsEventList.getReadWriteLock().writeLock().lock();
            try {
                studentsEventList.clear();
                studentsEventList.addAll(Arrays.asList(studentsNames));
            } finally {
                studentsEventList.getReadWriteLock().writeLock().unlock();
            }

            if (selectedStudent != null && studentsEventList.contains(selectedStudent)) {
                studentCombo.setSelectedItem(selectedStudent);
            }
        }
    }

    private void setButtonMargin(AbstractButton button) {
        Insets insets = button.getMargin();
        insets.left = 0;
        insets.right = 0;
        button.setMargin(insets);
    }

    private JPanel buildTopPanel(SearchSlotsTableModel tableModel) {
        // --- ИЗМЕНЕНИЕ 1: Используем BoxLayout ---
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));

        // Добавляем отступ слева
        topPanel.add(Box.createHorizontalStrut(5));

        JToggleButton selectAllSlots = selectAllSlotsButton("Вибрати всі", tableModel);
        setButtonMargin(selectAllSlots);

        JButton searchByInstructorAndStudentButton = getSearchByInstructorAndStudentButton("Пошук Разом", tableModel);
        setButtonMargin(searchByInstructorAndStudentButton);

        JButton searchByInstructorButton = getSearchByInstructorButton("Пошук за інструктором", tableModel);
        setButtonMargin(searchByInstructorButton);

        JButton searchByStudentButton = getSearchByStudentButton("Пошук за ученицею", tableModel);
        setButtonMargin(searchByStudentButton);

        JButton singleSlotButton = getAddSingleSlotButton("Додати вікно");
        setButtonMargin(singleSlotButton);

        topPanel.add(selectAllSlots);
        topPanel.add(Box.createHorizontalStrut(2));
        topPanel.add(new JLabel("Інструктор:"));
        topPanel.add(Box.createHorizontalStrut(2));
        topPanel.add(instructorCombo);
        topPanel.add(Box.createHorizontalStrut(2));
        topPanel.add(new JLabel("Учениця:"));
        topPanel.add(Box.createHorizontalStrut(2));
        topPanel.add(studentCombo);
        topPanel.add(Box.createHorizontalStrut(2));
        topPanel.add(searchByStudentButton);
        topPanel.add(Box.createHorizontalStrut(2));
        topPanel.add(singleSlotButton);
        topPanel.add(Box.createHorizontalStrut(2));
        topPanel.add(searchByInstructorAndStudentButton);
        topPanel.add(Box.createHorizontalStrut(2));
        topPanel.add(searchByInstructorButton);
        topPanel.add(Box.createHorizontalStrut(2));

        return topPanel;
    }

    private JPanel createBottomPanel(SearchSlotsTableModel tableModel, JTable table) {
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

    private void updateSearchSlots(List<ScheduleSlot> updatedSearchSlots, SearchSlotsTableModel tableModel) {
        tableModel.updateSlots(updatedSearchSlots);
    }

    private void addStudentClickListener(JTable table, SearchSlotsTableModel tableModel) {
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

    private JButton getSearchByInstructorAndStudentButton(String name, SearchSlotsTableModel tableModel) {
        JButton searchInstructorAndStudentButton = new JButton(name);
        searchInstructorAndStudentButton.addActionListener(e -> {
            String instructorName = (String) instructorCombo.getSelectedItem();
            String studentName = (String) studentCombo.getSelectedItem();

            List<ScheduleSlot> result = scheduleSlotService.findByInstructorAndStudentNames(instructorName, studentName);
            updateSearchSlots(result, tableModel);
            lastSearchMode = SearchMode.BOTH;
        });

        return searchInstructorAndStudentButton;
    }

    private JButton getSearchByInstructorButton(String name, SearchSlotsTableModel tableModel) {
        JButton searchInstructorAndStudentButton = new JButton(name);
        searchInstructorAndStudentButton.addActionListener(e -> {
            String instructorName = (String) instructorCombo.getSelectedItem();

            List<ScheduleSlot> result = scheduleSlotService.findAllBookedSlotsByInstructorName(instructorName);
            updateSearchSlots(result, tableModel);
            lastSearchMode = SearchMode.INSTRUCTOR;
        });

        return searchInstructorAndStudentButton;
    }

    private JButton getSearchByStudentButton(String name, SearchSlotsTableModel tableModel) {
        JButton searchInstructorAndStudentButton = new JButton(name);
        searchInstructorAndStudentButton.addActionListener(e -> {
            String studentName = (String) studentCombo.getSelectedItem();

            List<ScheduleSlot> result = scheduleSlotService.findByStudentName(studentName);
            updateSearchSlots(result, tableModel);
            lastSearchMode = SearchMode.STUDENT;
        });

        return searchInstructorAndStudentButton;
    }

    private JButton getSaveButton(SearchSlotsTableModel tableModel, JTable table) {
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

            updateCombos();
            refreshAfterUpdate(tableModel);
        });
        return saveButton;
    }

    private JButton getCopyButton(SearchSlotsTableModel tableModel) {
        JButton copyButton = new JButton("Копіювати вибране");
        copyButton.addActionListener(e -> {
            java.util.List<ScheduleSlot> selectedSlots = tableModel.getSelectedSlots();
            if (selectedSlots.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Немає вибраних рядків", "Попередження", JOptionPane.WARNING_MESSAGE);
                return;
            }

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

    private JButton getFreeButton(SearchSlotsTableModel tableModel, JTable table) {
        JButton freeButton = new JButton("Звільнити місце");
        freeButton.addActionListener(e -> {
            if (table.isEditing()) table.getCellEditor().stopCellEditing();

            List<ScheduleSlot> selectedSlots = tableModel.getSelectedSlots();
            if (selectedSlots.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Немає вибраних слотів", "Попередження", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int freed = 0;
            for (ScheduleSlot slot : selectedSlots) {
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

            updateCombos();
            refreshAfterUpdate(tableModel);
        });
        return freeButton;
    }

    private void refreshAfterUpdate(SearchSlotsTableModel tableModel) {
        String instructorName = (String) instructorCombo.getSelectedItem();
        String studentName = (String) studentCombo.getSelectedItem();

        List<ScheduleSlot> updatedSlots = switch (lastSearchMode) {
            case BOTH -> scheduleSlotService.findByInstructorAndStudentNames(instructorName, studentName);
            case INSTRUCTOR -> scheduleSlotService.findAllBookedSlotsByInstructorName(instructorName);
            case STUDENT -> scheduleSlotService.findByStudentName(studentName);
            default -> List.of();
        };

        updateSearchSlots(updatedSlots, tableModel);
    }

    private JToggleButton selectAllSlotsButton(String name, SearchSlotsTableModel tableModel) {
        JToggleButton button = new JToggleButton(name);

        button.addActionListener(e -> {
            boolean selectAll = button.isSelected();
            tableModel.selectAll(selectAll);
        });

        return button;
    }

    public JButton getAddSingleSlotButton(String name) {
        return new SingleSlotButton(
                name,
                (JFrame) SwingUtilities.getWindowAncestor(this),
                scheduleSlotService,
                instructorService,
                carService,
                studentService
        );
    }
}
