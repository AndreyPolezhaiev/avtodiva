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

        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        JLabel label = new JLabel("Переглянути вікна на");
        numberField = new JTextField(5);
        daysButton = new JRadioButton("днів", true);
        JRadioButton weeksButton = new JRadioButton("тижнів");

        ButtonGroup group = new ButtonGroup();
        group.add(daysButton);
        group.add(weeksButton);

        gbc.gridx = 0; gbc.gridy = 0;
        add(label, gbc);

        gbc.gridx = 1;
        add(numberField, gbc);

        gbc.gridx = 2;
        add(daysButton, gbc);

        gbc.gridx = 3;
        add(weeksButton, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 5;
        add(createInstructorCarButtonGrid(), gbc);

        gbc.gridy = 2;
        add(createSlotsButton("Переглянути вільні вікна", (instructor, car, days) -> {
            List<ScheduleSlot> slots = scheduleSlotService.findFreeSlots(instructor, car, days);
            freeSlotsPanel.refreshFreeSlots(slots);
            mainFrame.showPanel(PanelName.FREE_SLOTS_PANEL.name());
        }), gbc);

        gbc.gridy = 3;
        add(createSlotsButton("Переглянути зайняті вікна", (instructor, car, days) -> {
            List<ScheduleSlot> slots = scheduleSlotService.findBookedSlots(instructor, car, days);
            bookedSlotsPanel.refreshBookedSlots(slots);
            mainFrame.showPanel(PanelName.BOOKED_SLOTS_PANEL.name());
        }), gbc);

        gbc.gridy = 4;
        add(createSlotsButton("Переглянути всі вікна", (instructor, car, days) -> {
            List<ScheduleSlot> slots = scheduleSlotService.findAllSlots(instructor, car, days);
            allSlotsPanel.refreshAllSlots(slots);
            mainFrame.showPanel(PanelName.ALL_SLOTS_PANEL.name());
        }), gbc);

        gbc.gridy = 5;
        add(showInstructorsWeekends("Переглянути інструкторів та їхні вихідні"), gbc);

        gbc.gridy = 6;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(10, 5, 5, 5);
        add(addInstructorButton("Додати або видалити інструктора"), gbc);

        gbc.gridy = 7;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(10, 5, 5, 5);
        add(addCarButton("Додати або видалити машину"), gbc);

        // 👉 новая кнопка: добавление окон для инструктора
        gbc.gridy = 8;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(10, 5, 5, 5);
        add(addWindowsForInstructorButton("Додати вільні місця для інструктора"), gbc);

        // 👉 новая кнопка: добавление окон для машины
        gbc.gridy = 9;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(10, 5, 5, 5);
        add(addWindowsForCarButton("Додати вільні місця для машини"), gbc);

        gbc.gridy = 10;
        gbc.insets = new Insets(20, 5, 5, 5);
        add(addFreeWindows("Додати вільні місця для запису (всі)"), gbc);

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
        JButton addInstructorBtn = new JButton(name);
        addInstructorBtn.addActionListener(e -> {
            AddCarDialog dialog = new AddCarDialog(
                    (JFrame) SwingUtilities.getWindowAncestor(this),
                    carService
            );
            dialog.setVisible(true);

            buildUI();
        });
        return addInstructorBtn;
    }

    private JButton addWindowsForInstructorButton(String name) {
        JButton button = new JButton(name);
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
