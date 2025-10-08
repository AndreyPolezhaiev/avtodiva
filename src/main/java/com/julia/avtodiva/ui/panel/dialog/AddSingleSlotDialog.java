package com.julia.avtodiva.ui.panel.dialog;

import com.julia.avtodiva.model.Car;
import com.julia.avtodiva.model.Instructor;
import com.julia.avtodiva.model.ScheduleSlot;
import com.julia.avtodiva.model.Student;
import com.julia.avtodiva.service.car.CarService;
import com.julia.avtodiva.service.instructor.InstructorService;
import com.julia.avtodiva.service.schedule.ScheduleSlotService;
import com.julia.avtodiva.service.student.StudentService;
import org.jdesktop.swingx.JXDatePicker;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class AddSingleSlotDialog extends JDialog {
    private final ScheduleSlotService scheduleSlotService;
    private final InstructorService instructorService;
    private final CarService carService;
    private final StudentService studentService;

    // UI компоненты
    private JXDatePicker datePicker;
    private JComboBox<LocalTime> timeFromCombo;
    private JTextField timeToField;
    private JComboBox<String> instructorCombo;
    private JComboBox<String> carCombo;
    private JTextField studentField;
    private JTextArea descArea;
    private JTextArea linkArea;

    public AddSingleSlotDialog(JFrame owner, ScheduleSlotService scheduleSlotService, InstructorService instructorService, CarService carService, StudentService studentService) {
        super(owner, "Додати виключне вікно", true);
        this.scheduleSlotService = scheduleSlotService;
        this.instructorService = instructorService;
        this.carService = carService;
        this.studentService = studentService;

        buildUI();
        pack();
        setLocationRelativeTo(owner);
    }

    private void buildUI() {
        // Код построения UI остается без изменений, он корректен.
        setLayout(new BorderLayout(10, 10));

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        datePicker = new JXDatePicker();
        datePicker.setDate(new java.util.Date());

        timeFromCombo = createTimeFromComboBox();
        timeFromCombo.addActionListener(e -> updateEndTime());

        timeToField = new JTextField(10);
        timeToField.setEditable(false);
        timeToField.setFont(timeToField.getFont().deriveFont(Font.BOLD));

        instructorCombo = new JComboBox<>(instructorService.getInstructorsNames());
        carCombo = new JComboBox<>(carService.getCarsNames());
        studentField = new JTextField(20);

        descArea = new JTextArea(4, 30);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        JScrollPane descScroll = new JScrollPane(descArea);

        linkArea = new JTextArea(2, 30);
        linkArea.setLineWrap(true);
        linkArea.setWrapStyleWord(true);
        JScrollPane linkScroll = new JScrollPane(linkArea);

        gbc.gridx = 0; gbc.gridy = 0; formPanel.add(new JLabel("Дата:"), gbc);
        gbc.gridx = 1; formPanel.add(datePicker, gbc);
        gbc.gridx = 0; gbc.gridy++; formPanel.add(new JLabel("Час з:"), gbc);
        gbc.gridx = 1; formPanel.add(timeFromCombo, gbc);
        gbc.gridx = 0; gbc.gridy++; formPanel.add(new JLabel("Час до:"), gbc);
        gbc.gridx = 1; formPanel.add(timeToField, gbc);
        gbc.gridx = 0; gbc.gridy++; formPanel.add(new JLabel("Інструктор:"), gbc);
        gbc.gridx = 1; formPanel.add(instructorCombo, gbc);
        gbc.gridx = 0; gbc.gridy++; formPanel.add(new JLabel("Машина:"), gbc);
        gbc.gridx = 1; formPanel.add(carCombo, gbc);
        gbc.gridx = 0; gbc.gridy++; formPanel.add(new JLabel("Учениця (обов'язково):"), gbc);
        gbc.gridx = 1; formPanel.add(studentField, gbc);
        gbc.gridx = 0; gbc.gridy++; formPanel.add(new JLabel("Опис:"), gbc);
        gbc.gridx = 1; formPanel.add(descScroll, gbc);
        gbc.gridx = 0; gbc.gridy++; formPanel.add(new JLabel("Посилання:"), gbc);
        gbc.gridx = 1; formPanel.add(linkScroll, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveButton = new JButton("Зберегти");
        saveButton.addActionListener(e -> saveSlot());
        JButton cancelButton = new JButton("Скасувати");
        cancelButton.addActionListener(e -> dispose());
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        updateEndTime();
    }

    private JComboBox<LocalTime> createTimeFromComboBox() {
        JComboBox<LocalTime> comboBox = new JComboBox<>();
        comboBox.addItem(LocalTime.of(8, 0));
        comboBox.addItem(LocalTime.of(11, 30));
        comboBox.addItem(LocalTime.of(15, 0));
        comboBox.addItem(LocalTime.of(18, 15));
        return comboBox;
    }

    private void updateEndTime() {
        LocalTime selectedTimeFrom = (LocalTime) timeFromCombo.getSelectedItem();
        if (selectedTimeFrom == null) {
            return;
        }

        LocalTime timeTo;
        if (selectedTimeFrom.isAfter(LocalTime.of(18, 14))) {
            timeTo = selectedTimeFrom.plusHours(2);
        } else {
            timeTo = selectedTimeFrom.plusHours(3);
        }

        timeToField.setText(timeTo.format(DateTimeFormatter.ofPattern("HH:mm")));
    }

    private void saveSlot() {
        // 1. Валидация данных (остается без изменений)
        if (datePicker.getDate() == null || instructorCombo.getSelectedItem() == null || carCombo.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Дата, інструктор і машина є обов'язковими полями.", "Помилка валідації", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 2. Сбор данных из формы
        LocalDate date = datePicker.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalTime timeFrom = (LocalTime) timeFromCombo.getSelectedItem();
        LocalTime timeTo;
        if (timeFrom.isAfter(LocalTime.of(18, 14))) {
            timeTo = timeFrom.plusHours(2);
        } else {
            timeTo = timeFrom.plusHours(3);
        }
        String instructorName = (String) instructorCombo.getSelectedItem();
        String carName = (String) carCombo.getSelectedItem();
        String studentName = studentField.getText().trim();
        String description = descArea.getText();
        String link = linkArea.getText();

        // --- Применяем подход из TableModel: создаем объекты напрямую ---
        Instructor instructor = new Instructor();
        instructor.setName(instructorName);

        Car car = new Car();
        car.setName(carName);

        if (timeFrom.isAfter(timeTo)) {
            JOptionPane.showMessageDialog(this, "Час 'ДО' не може бути раніше за час 'З'", "Помилка валідації", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 3. Создание и наполнение объекта ScheduleSlot
        ScheduleSlot newSlot = new ScheduleSlot();
        newSlot.setDate(date);
        newSlot.setTimeFrom(timeFrom);
        newSlot.setTimeTo(timeTo);
        newSlot.setInstructor(instructor);
        newSlot.setCar(car);
        newSlot.setDescription(description);
        newSlot.setLink(link);

        // --- Логика из setValueAt и getSelectedSlots: работа со студентом и статусом брони ---
        if (!studentName.isEmpty()) {
            Student student = new Student();
            student.setName(studentName);
            newSlot.setStudent(student);
            newSlot.setBooked(true);
        } else {
            JOptionPane.showMessageDialog(this, "Не можна зберегти слот з порожньою ученицею", "Помилка", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 4. Сохранение через сервис
        try {
            // Используем метод rescheduleSlot, который, судя по вашему коду, отвечает за сохранение/обновление
            scheduleSlotService.createSlot(newSlot);
            JOptionPane.showMessageDialog(this, "Слот успішно створено!", "Успіх", JOptionPane.INFORMATION_MESSAGE);
            dispose(); // Закрываем диалог
        } catch (Exception ex) {
            // Этот блок перехватит ошибки, если, например, слот уже занят
            JOptionPane.showMessageDialog(this, "Помилка при збереженні слота: " + ex.getMessage(), "Помилка", JOptionPane.ERROR_MESSAGE);
        }
    }
}