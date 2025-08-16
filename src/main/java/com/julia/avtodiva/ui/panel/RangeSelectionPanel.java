package com.julia.avtodiva.ui.panel;

import com.julia.avtodiva.model.ScheduleSlot;
import com.julia.avtodiva.service.schedule.ScheduleSlotService;
import com.julia.avtodiva.service.window.WindowService;
import com.julia.avtodiva.ui.MainFrame;
import com.julia.avtodiva.ui.model.PanelName;
import com.julia.avtodiva.ui.panel.data.AllSlotsPanel;
import com.julia.avtodiva.ui.panel.data.BookedSlotsPanel;
import com.julia.avtodiva.ui.panel.data.FreeSlotsPanel;
import com.julia.avtodiva.ui.panel.dialog.AddWindowsDialog;
import com.julia.avtodiva.ui.panel.dialog.BookWindowDialog;
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
    private final JTextField numberField;
    private final JRadioButton daysButton;
    private final MainFrame mainFrame;
    private final AppState appState;
    @Autowired
    private WindowService windowService;
    private final ScheduleSlotService scheduleSlotService;
    private final AllSlotsPanel allSlotsPanel;
    private final FreeSlotsPanel freeSlotsPanel;
    private final BookedSlotsPanel bookedSlotsPanel;

    private final List<JToggleButton> instructorButtons = new ArrayList<>();
    private final List<JToggleButton> carButtons = new ArrayList<>();

    @Autowired
    public RangeSelectionPanel(@Lazy MainFrame mainFrame, AppState appState, ScheduleSlotService scheduleSlotService, AllSlotsPanel allSlotsPanel, FreeSlotsPanel freeSlotsPanel, BookedSlotsPanel bookedSlotsPanel) {
        this.mainFrame = mainFrame;
        this.appState = appState;
        this.scheduleSlotService = scheduleSlotService;
        this.allSlotsPanel = allSlotsPanel;
        this.freeSlotsPanel = freeSlotsPanel;
        this.bookedSlotsPanel = bookedSlotsPanel;

        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        JLabel label = new JLabel("Просмотреть окна на");
        numberField = new JTextField(5);
        daysButton = new JRadioButton("дней", true);
        JRadioButton weeksButton = new JRadioButton("недель");

        ButtonGroup group = new ButtonGroup();
        group.add(daysButton);
        group.add(weeksButton);

        // Строка 0
        gbc.gridx = 0; gbc.gridy = 0;
        add(label, gbc);

        gbc.gridx = 1;
        add(numberField, gbc);

        gbc.gridx = 2;
        add(daysButton, gbc);

        gbc.gridx = 3;
        add(weeksButton, gbc);

        // Строка 1 (панель кнопок)
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 5;
        add(createInstructorCarButtonGrid(), gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 5;
        add(showFreeSlots("Просмотреть свободные окна"), gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 5;
        add(showBookedSlots("Просмотреть занятые окна"), gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 5;
        add(showAllSlots("Просмотреть все окна"), gbc);

        // Добавить окно
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 5;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(20, 5, 5, 5);
        add(bookWindow("Добавить запись"), gbc);

        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 5;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(20, 5, 5, 5);
        add(addFreeWindows("Добавить свободные места для записей"), gbc);
    }

    private JButton showBookedSlots(String name) {
        JButton viewButton = new JButton(name);
        viewButton.addActionListener(e -> {
            try {
                int value = Integer.parseInt(numberField.getText().trim());
                if (value <= 0) throw new NumberFormatException();

                String selectedInstructor = instructorButtons.stream()
                        .filter(AbstractButton::isSelected)
                        .map(b -> b.getText().toLowerCase())
                        .findFirst()
                        .orElse(null);

                String selectedCar = carButtons.stream()
                        .filter(AbstractButton::isSelected)
                        .map(b -> b.getText().toLowerCase())
                        .findFirst()
                        .orElse(null);

                if (selectedInstructor == null || selectedCar == null) {
                    JOptionPane.showMessageDialog(this, "Выберите и инструктора, и машину", "Ошибка выбора", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                AppState.daysAhead = daysButton.isSelected() ? value : value * 7;
                AppState.instructorName = selectedInstructor;
                AppState.carName = selectedCar;

                String instructor = AppState.instructorName;
                String car = AppState.carName;
                int days = AppState.daysAhead;

                List<ScheduleSlot> bookedSlots = scheduleSlotService.findBookedSlots(instructor, car, days);
                bookedSlotsPanel.refreshBookedSlots(bookedSlots);

                mainFrame.showPanel(PanelName.BOOKED_SLOTS_PANEL.name());

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Введите корректное положительное число.", "Ошибка ввода", JOptionPane.ERROR_MESSAGE);
            }
        });
        return viewButton;
    }

    private JButton showFreeSlots(String name) {
        JButton viewButton = new JButton(name);
        viewButton.addActionListener(e -> {
            try {
                int value = Integer.parseInt(numberField.getText().trim());
                if (value <= 0) throw new NumberFormatException();

                String selectedInstructor = instructorButtons.stream()
                        .filter(AbstractButton::isSelected)
                        .map(b -> b.getText().toLowerCase())
                        .findFirst()
                        .orElse(null);

                String selectedCar = carButtons.stream()
                        .filter(AbstractButton::isSelected)
                        .map(b -> b.getText().toLowerCase())
                        .findFirst()
                        .orElse(null);

                if (selectedInstructor == null || selectedCar == null) {
                    JOptionPane.showMessageDialog(this, "Выберите и инструктора, и машину", "Ошибка выбора", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                AppState.daysAhead = daysButton.isSelected() ? value : value * 7;
                AppState.instructorName = selectedInstructor;
                AppState.carName = selectedCar;

                String instructor = AppState.instructorName;
                String car = AppState.carName;
                int days = AppState.daysAhead;

                List<ScheduleSlot> freeSlots = scheduleSlotService.findFreeSlots(instructor, car, days);
                freeSlotsPanel.refreshFreeSlots(freeSlots);

                mainFrame.showPanel(PanelName.FREE_SLOTS_PANEL.name());

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Введите корректное положительное число.", "Ошибка ввода", JOptionPane.ERROR_MESSAGE);
            }
        });
        return viewButton;
    }

    private JButton showAllSlots(String name) {
        JButton viewButton = new JButton(name);
        viewButton.addActionListener(e -> {
            try {
                int value = Integer.parseInt(numberField.getText().trim());
                if (value <= 0) throw new NumberFormatException();

                String selectedInstructor = instructorButtons.stream()
                        .filter(AbstractButton::isSelected)
                        .map(b -> b.getText().toLowerCase())
                        .findFirst()
                        .orElse(null);

                String selectedCar = carButtons.stream()
                        .filter(AbstractButton::isSelected)
                        .map(b -> b.getText().toLowerCase())
                        .findFirst()
                        .orElse(null);

                if (selectedInstructor == null || selectedCar == null) {
                    JOptionPane.showMessageDialog(this, "Выберите и инструктора, и машину", "Ошибка выбора", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                AppState.daysAhead = daysButton.isSelected() ? value : value * 7;
                AppState.instructorName = selectedInstructor;
                AppState.carName = selectedCar;

                String instructor = AppState.instructorName;
                String car = AppState.carName;
                int days = AppState.daysAhead;

                List<ScheduleSlot> allSlots = scheduleSlotService.findAllSlots(instructor, car, days);
                allSlotsPanel.refreshAllSlots(allSlots);

                mainFrame.showPanel(PanelName.ALL_SLOTS_PANEL.name());

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Введите корректное положительное число.", "Ошибка ввода", JOptionPane.ERROR_MESSAGE);
            }
        });
        return viewButton;
    }

    private JPanel createInstructorCarButtonGrid() {
        JPanel gridPanel = new JPanel(new GridLayout(5, 2, 10, 10)); // 5 строк

        String[] instructors = {"Таня", "Марина", "Христина", "Ксюша", "Іра"};
        String[] cars = {"Фея", "Мерлин", "Мавка", "Фрея", "На своїй"};

        ButtonGroup instructorGroup = new ButtonGroup();
        ButtonGroup carGroup = new ButtonGroup();

        for (int i = 0; i < 5; i++) {
            JToggleButton instructorButton = new JToggleButton(instructors[i]);
            JToggleButton carButton = new JToggleButton(cars[i]);

            instructorGroup.add(instructorButton);
            carGroup.add(carButton);

            instructorButtons.add(instructorButton);
            carButtons.add(carButton);

            gridPanel.add(instructorButton);
            gridPanel.add(carButton);
        }

        return gridPanel;
    }

    private JButton bookWindow(String name) {
        JButton addWindowButton = new JButton(name);
        addWindowButton.addActionListener(e -> {
            BookWindowDialog dialog = new BookWindowDialog((JFrame) SwingUtilities.getWindowAncestor(this), windowService);
            dialog.setVisible(true);
        });

        return addWindowButton;
    }

    private JButton addFreeWindows(String name) {
        JButton addWindowButton = new JButton(name);
        addWindowButton.addActionListener(e -> {
            AddWindowsDialog dialog = new AddWindowsDialog((JFrame) SwingUtilities.getWindowAncestor(this), windowService);
            dialog.setVisible(true);
        });

        return addWindowButton;
    }
}
