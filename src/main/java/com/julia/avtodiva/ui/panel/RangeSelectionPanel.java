package com.julia.avtodiva.ui.panel;

import com.julia.avtodiva.model.Instructor;
import com.julia.avtodiva.model.ScheduleSlot;
import com.julia.avtodiva.service.car.CarService;
import com.julia.avtodiva.service.instructor.InstructorService;
import com.julia.avtodiva.service.schedule.ScheduleSlotService;
import com.julia.avtodiva.service.student.StudentService;
import com.julia.avtodiva.service.window.WindowService;
import com.julia.avtodiva.ui.MainFrame;
import com.julia.avtodiva.ui.model.PanelName;
import com.julia.avtodiva.ui.panel.data.*;
import com.julia.avtodiva.ui.panel.dialog.*;
import com.julia.avtodiva.ui.state.AppState;
import com.julia.avtodiva.ui.util.SingleSlotButton;
import org.jdesktop.swingx.JXDatePicker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.*;
import java.sql.Date;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Component
public class RangeSelectionPanel extends JPanel {
    private final MainFrame mainFrame;
    @Autowired
    private WindowService windowService;
    private final ScheduleSlotService scheduleSlotService;
    private final CarService carService;
    private final AllSlotsPanel allSlotsPanel;
    private final FreeSlotsPanel freeSlotsPanel;
    private final BookedSlotsPanel bookedSlotsPanel;
    private final SearchSlotsPanel searchSlotsPanel;
    private final InstructorWeekendsPanel instructorWeekendsPanel;
    @Autowired
    private final InstructorService instructorService;
    @Autowired
    private final StudentService studentService;

    private final List<JToggleButton> instructorButtons = new ArrayList<>();
    private final List<JToggleButton> carButtons = new ArrayList<>();

    private JXDatePicker startDatePicker;
    private JXDatePicker endDatePicker;

    @Autowired
    public RangeSelectionPanel(@Lazy MainFrame mainFrame,
                               ScheduleSlotService scheduleSlotService, CarService carService,
                               AllSlotsPanel allSlotsPanel,
                               FreeSlotsPanel freeSlotsPanel,
                               BookedSlotsPanel bookedSlotsPanel, SearchSlotsPanel searchSlotsPanel,
                               InstructorWeekendsPanel instructorWeekendsPanel,
                               InstructorService instructorService, StudentService studentService) {
        this.mainFrame = mainFrame;
        this.scheduleSlotService = scheduleSlotService;
        this.carService = carService;
        this.allSlotsPanel = allSlotsPanel;
        this.freeSlotsPanel = freeSlotsPanel;
        this.bookedSlotsPanel = bookedSlotsPanel;
        this.searchSlotsPanel = searchSlotsPanel;
        this.instructorWeekendsPanel = instructorWeekendsPanel;
        this.instructorService = instructorService;
        this.studentService = studentService;

        buildUI();
    }

    private void buildUI() {
        removeAll();
        setLayout(new BorderLayout(10, 10));

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("Період:"));

        startDatePicker = new JXDatePicker();
        startDatePicker.setFormats("dd.MM.yyyy");
        startDatePicker.setDate(Date.valueOf(LocalDate.now()));
        topPanel.add(startDatePicker);

        topPanel.add(new JLabel("—"));

        endDatePicker = new JXDatePicker();
        endDatePicker.setFormats("dd.MM.yyyy");
        endDatePicker.setDate(Date.valueOf(LocalDate.now().plusDays(7)));
        topPanel.add(endDatePicker);
        topPanel.add(getAddSingleSlotButton("Додати вікно"));

        add(topPanel, BorderLayout.NORTH);

        // Центральная часть: сетка с двумя колонками
        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 20, 0));

        // Левая колонка — работа со слотами
        JPanel slotsPanel = new JPanel(new GridLayout(5, 1, 5, 5));
        slotsPanel.setBorder(BorderFactory.createTitledBorder("Вікна"));
        slotsPanel.add(createSlotsButton("Переглянути вільні вікна", (i, c, start, end) -> {
            List<ScheduleSlot> slots = scheduleSlotService.findFreeSlots(i, c, start, end);
            freeSlotsPanel.refreshFreeSlots(slots);
            mainFrame.showPanel(PanelName.FREE_SLOTS_PANEL.name());
        }));
        slotsPanel.add(createSlotsButton("Переглянути зайняті вікна", (i, c, start, end) -> {
            List<ScheduleSlot> slots = scheduleSlotService.findBookedSlots(i, c, start, end);
            bookedSlotsPanel.refreshBookedSlots(slots);
            mainFrame.showPanel(PanelName.BOOKED_SLOTS_PANEL.name());
        }));
        slotsPanel.add(createSlotsButton("Переглянути всі вікна", (i, c, start, end) -> {
            List<ScheduleSlot> slots = scheduleSlotService.findAllSlots(i, c, start, end);
            allSlotsPanel.refreshAllSlots(slots);
            mainFrame.showPanel(PanelName.ALL_SLOTS_PANEL.name());
        }));
        slotsPanel.add(showInstructorsWeekends("Вихідні інструкторів"));
        slotsPanel.add(showSearchPanelButton("Пошук за інструктором/ученицею"));

        // Правая колонка — управление справочниками
        JPanel managePanel = new JPanel(new GridLayout(5, 1, 5, 5));
        managePanel.setBorder(BorderFactory.createTitledBorder("Справочники"));
        managePanel.add(addInstructorButton("Додати / видалити інструктора"));
        managePanel.add(addCarButton("Додати / видалити машину"));
        managePanel.add(addWindowsForInstructorButton("Додати місця для інструктора"));
        managePanel.add(addWindowsForCarButton("Додати місця для машини"));
        managePanel.add(addFreeWindows("Додати вільні місця (всі)"));

        centerPanel.add(slotsPanel);
        centerPanel.add(managePanel);

        add(centerPanel, BorderLayout.SOUTH);

        // Нижняя часть: выбор инструктора и машины
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBorder(BorderFactory.createTitledBorder("Вибір інструкторів і машин"));
        bottomPanel.add(createInstructorCarButtonGrid(), BorderLayout.CENTER);

        add(bottomPanel, BorderLayout.CENTER);

        revalidate();
        repaint();
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

    @FunctionalInterface
    private interface SlotAction {
        void run(List<String> instructors, List<String> cars, LocalDate start, LocalDate end);
    }

    private JButton showSearchPanelButton(String name) {
        JButton button = new JButton(name);
        button.addActionListener(e -> {
            searchSlotsPanel.refreshSearchSlots();
            mainFrame.showPanel(PanelName.SEARCH_SLOTS_PANEL.name());
        });
        return button;
    }

    private JButton createSlotsButton(String name, SlotAction action) {
        JButton button = new JButton(name);
        button.addActionListener(e -> {
            try {
                LocalDate start = startDatePicker.getDate()
                        .toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                LocalDate end = endDatePicker.getDate()
                        .toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

                List<String> selectedInstructors = instructorButtons.stream()
                        .filter(AbstractButton::isSelected)
                        .map(AbstractButton::getText)
                        .toList();

                List<String> selectedCars = carButtons.stream()
                        .filter(AbstractButton::isSelected)
                        .map(AbstractButton::getText)
                        .toList();

                if (selectedInstructors.isEmpty() || selectedCars.isEmpty()) {
                    JOptionPane.showMessageDialog(this,
                            "Виберіть хоча б одного інструктора і машину",
                            "Помилка вибору",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }

                if (end.isBefore(start)) {
                    JOptionPane.showMessageDialog(this,
                            "Дата завершення не може бути раніше дати початку.",
                            "Помилка вибору дат",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                AppState.startDate = start;
                AppState.endDate = end;
                AppState.instructorNames = selectedInstructors;
                AppState.carNames = selectedCars;

                action.run(selectedInstructors, selectedCars, start, end);

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this,
                        "Помилка при виборі дат: " + ex.getMessage(),
                        "Помилка",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
        return button;
    }

    private JButton showInstructorsWeekends(String name) {
        JButton viewButton = new JButton(name);
        viewButton.addActionListener(e -> {
            String selectedInstructor = instructorButtons.stream()
                    .filter(AbstractButton::isSelected)
                    .map(AbstractButton::getText)
                    .findFirst()
                    .orElse(null);

            if (selectedInstructor == null) {
                JOptionPane.showMessageDialog(this,
                        "Виберіть інструктора",
                        "Помилка вибору",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            AppState.instructorNames = List.of(selectedInstructor);

            Instructor instructor = instructorService.findByName(selectedInstructor);
            instructorWeekendsPanel.refreshInstructor(instructor);

            mainFrame.showPanel(PanelName.INSTRUCTOR_WEEKEND_PANEL.name());
        });
        return viewButton;
    }

    private JPanel createInstructorCarButtonGrid() {
        instructorButtons.clear();
        carButtons.clear();

        int instructorCount = instructorService.getInstructorsNames().length;
        int carCount = carService.getCarsNames().length;
        int max = Math.max(instructorCount, carCount);

        JPanel gridPanel = new JPanel(new GridLayout(max, 2, 10, 10));

        for (int i = 0; i < max; i++) {
            // Левая колонка — инструкторы
            if (i < instructorCount) {
                String instructorName = instructorService.getInstructorsNames()[i];
                String instructorButtonName = instructorName.substring(0, 1).toUpperCase() + instructorName.substring(1);
                JToggleButton b = new JToggleButton(instructorButtonName);
                instructorButtons.add(b);
                gridPanel.add(b);
            } else {
                gridPanel.add(new JLabel("")); // пустая ячейка
            }

            // Правая колонка — машины
            if (i < carCount) {
                String carName = carService.getCarsNames()[i];
                String carButtonName = carName.substring(0, 1).toUpperCase() + carName.substring(1);
                JToggleButton b = new JToggleButton(carButtonName);
                carButtons.add(b);
                gridPanel.add(b);
            } else {
                gridPanel.add(new JLabel("")); // пустая ячейка
            }
        }

        return gridPanel;
    }

    private JButton addFreeWindows(String name) {
        JButton addWindowButton = new JButton(name);
        addWindowButton.setBackground(new Color(0, 100, 0)); // тёмно-зелёный
        addWindowButton.setForeground(Color.WHITE);
        addWindowButton.addActionListener(e -> {
            AddWindowsDialog dialog = new AddWindowsDialog(
                    (JFrame) SwingUtilities.getWindowAncestor(this),
                    windowService
            );
            dialog.setVisible(true);
        });
        return addWindowButton;
    }

    private JButton addInstructorButton(String name) {
        JButton addInstructorBtn = new JButton(name);
        addInstructorBtn.setBackground(new Color(220, 53, 69)); // bootstrap red
        addInstructorBtn.setForeground(Color.WHITE);
        addInstructorBtn.setFocusPainted(false);
        addInstructorBtn.setOpaque(true);

        addInstructorBtn.addActionListener(e -> {
            AddInstructorDialog dialog = new AddInstructorDialog(
                    (JFrame) SwingUtilities.getWindowAncestor(this),
                    instructorService
            );
            dialog.setVisible(true);

            buildUI();
        });
        return addInstructorBtn;
    }

    private JButton addCarButton(String name) {
        JButton addCarBtn = new JButton(name);
        addCarBtn.setBackground(new Color(220, 53, 69)); // bootstrap red
        addCarBtn.setForeground(Color.WHITE);
        addCarBtn.setFocusPainted(false);
        addCarBtn.setOpaque(true);

        addCarBtn.addActionListener(e -> {
            AddCarDialog dialog = new AddCarDialog(
                    (JFrame) SwingUtilities.getWindowAncestor(this),
                    carService
            );
            dialog.setVisible(true);

            buildUI();
        });
        return addCarBtn;
    }

    private JButton addWindowsForInstructorButton(String name) {
        JButton button = new JButton(name);
        button.setBackground(new Color(40, 167, 69)); // bootstrap green
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.addActionListener(e -> {
            AddWindowsForInstructorDialog dialog = new AddWindowsForInstructorDialog(
                    (JFrame) SwingUtilities.getWindowAncestor(this),
                    windowService,
                    instructorService
            );
            dialog.setVisible(true);
            buildUI(); // обновим панель после добавления
        });
        return button;
    }

    private JButton addWindowsForCarButton(String name) {
        JButton button = new JButton(name);
        button.setBackground(new Color(40, 167, 69)); // bootstrap green
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.addActionListener(e -> {
            AddWindowsForCarDialog dialog = new AddWindowsForCarDialog(
                    (JFrame) SwingUtilities.getWindowAncestor(this),
                    windowService,
                    carService
            );
            dialog.setVisible(true);
            buildUI(); // обновим панель после добавления
        });
        return button;
    }


}
