package com.julia.avtodiva.ui.panel;

import com.julia.avtodiva.model.Instructor;
import com.julia.avtodiva.model.ScheduleSlot;
import com.julia.avtodiva.service.car.CarService;
import com.julia.avtodiva.service.instructor.InstructorService;
import com.julia.avtodiva.service.schedule.ScheduleSlotService;
import com.julia.avtodiva.service.window.WindowService;
import com.julia.avtodiva.ui.MainFrame;
import com.julia.avtodiva.ui.model.PanelName;
import com.julia.avtodiva.ui.panel.data.AllSlotsPanel;
import com.julia.avtodiva.ui.panel.data.BookedSlotsPanel;
import com.julia.avtodiva.ui.panel.data.FreeSlotsPanel;
import com.julia.avtodiva.ui.panel.data.InstructorsPanel;
import com.julia.avtodiva.ui.panel.dialog.*;
import com.julia.avtodiva.ui.state.AppState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

@Component
public class RangeSelectionPanel extends JPanel {
    private JTextField numberField;
    private JRadioButton daysButton;
    private final MainFrame mainFrame;
    @Autowired
    private WindowService windowService;
    private final ScheduleSlotService scheduleSlotService;
    private final CarService carService;
    private final AllSlotsPanel allSlotsPanel;
    private final FreeSlotsPanel freeSlotsPanel;
    private final BookedSlotsPanel bookedSlotsPanel;
    private final InstructorsPanel instructorsPanel;
    @Autowired
    private final InstructorService instructorService;

    private final List<JToggleButton> instructorButtons = new ArrayList<>();
    private final List<JToggleButton> carButtons = new ArrayList<>();

    @Autowired
    public RangeSelectionPanel(@Lazy MainFrame mainFrame,
                               ScheduleSlotService scheduleSlotService, CarService carService,
                               AllSlotsPanel allSlotsPanel,
                               FreeSlotsPanel freeSlotsPanel,
                               BookedSlotsPanel bookedSlotsPanel,
                               InstructorsPanel instructorsPanel,
                               InstructorService instructorService) {
        this.mainFrame = mainFrame;
        this.scheduleSlotService = scheduleSlotService;
        this.carService = carService;
        this.allSlotsPanel = allSlotsPanel;
        this.freeSlotsPanel = freeSlotsPanel;
        this.bookedSlotsPanel = bookedSlotsPanel;
        this.instructorsPanel = instructorsPanel;
        this.instructorService = instructorService;

        buildUI();
    }

    private void buildUI() {
        removeAll();
        setLayout(new BorderLayout(10, 10));

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("Переглянути вікна на"));
        numberField = new JTextField(5);
        topPanel.add(numberField);
        daysButton = new JRadioButton("днів", true);
        JRadioButton weeksButton = new JRadioButton("тижнів");
        ButtonGroup group = new ButtonGroup();
        group.add(daysButton);
        group.add(weeksButton);
        topPanel.add(daysButton);
        topPanel.add(weeksButton);

        add(topPanel, BorderLayout.NORTH);

        // Центральная часть: сетка с двумя колонками
        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 20, 0));

        // Левая колонка — работа со слотами
        JPanel slotsPanel = new JPanel(new GridLayout(4, 1, 5, 5));
        slotsPanel.setBorder(BorderFactory.createTitledBorder("Вікна"));
        slotsPanel.add(createSlotsButton("Переглянути вільні вікна", (i, c, d) -> {
            List<ScheduleSlot> slots = scheduleSlotService.findFreeSlots(i, c, d);
            freeSlotsPanel.refreshFreeSlots(slots);
            mainFrame.showPanel(PanelName.FREE_SLOTS_PANEL.name());
        }));
        slotsPanel.add(createSlotsButton("Переглянути зайняті вікна", (i, c, d) -> {
            List<ScheduleSlot> slots = scheduleSlotService.findBookedSlots(i, c, d);
            bookedSlotsPanel.refreshBookedSlots(slots);
            mainFrame.showPanel(PanelName.BOOKED_SLOTS_PANEL.name());
        }));
        slotsPanel.add(createSlotsButton("Переглянути всі вікна", (i, c, d) -> {
            List<ScheduleSlot> slots = scheduleSlotService.findAllSlots(i, c, d);
            allSlotsPanel.refreshAllSlots(slots);
            mainFrame.showPanel(PanelName.ALL_SLOTS_PANEL.name());
        }));
        slotsPanel.add(showInstructorsWeekends("Вихідні інструкторів"));

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

    @FunctionalInterface
    private interface SlotAction {
        void run(List<String> instructors, List<String> cars, int days);
    }

    private JButton createSlotsButton(String name, SlotAction action) {
        JButton button = new JButton(name);
        button.addActionListener(e -> {
            try {
                int value = Integer.parseInt(numberField.getText().trim());
                if (value <= 0) throw new NumberFormatException();

                List<String> selectedInstructors = instructorButtons.stream()
                        .filter(AbstractButton::isSelected)
                        .map(b -> b.getText().toLowerCase())
                        .toList();

                List<String> selectedCars = carButtons.stream()
                        .filter(AbstractButton::isSelected)
                        .map(b -> b.getText().toLowerCase())
                        .toList();

                if (selectedInstructors.isEmpty() || selectedCars.isEmpty()) {
                    JOptionPane.showMessageDialog(this,
                            "Виберіть хоча б одного інструктора і машину",
                            "Помилка вибору",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }

                AppState.daysAhead = daysButton.isSelected() ? value : value * 7;

                AppState.instructorNames = selectedInstructors;
                AppState.carNames = selectedCars;

                action.run(selectedInstructors, selectedCars, AppState.daysAhead);

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this,
                        "Введіть коректне позитивне число.",
                        "Помилка вводу",
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
                    .map(b -> b.getText().toLowerCase())
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
            instructorsPanel.refreshInstructor(instructor);

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
