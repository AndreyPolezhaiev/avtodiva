package com.julia.avtodiva.ui.panel.dialog;

import com.julia.avtodiva.model.Window;
import com.julia.avtodiva.service.window.WindowService;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class BookWindowDialog extends JDialog {

    public BookWindowDialog(JFrame parent, WindowService windowService) {
        super(parent, "Добавить новое окно", true);
        setSize(400, 300);
        setLocationRelativeTo(parent);
        setLayout(new GridLayout(5, 2, 10, 10));

        String[] instructors = {"Таня", "Марина", "Христина", "Ксюша", "Іра"};
        String[] cars = {"Фея", "Мерлин", "Мавка", "Фрея", "На своїй"};

        JComboBox<String> instructorBox = new JComboBox<>(instructors);
        JComboBox<String> carBox = new JComboBox<>(cars);

        // Создание списка дат на 30 дней вперёд
        List<String> dateOptions = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (int i = 0; i < 60; i++) {
            LocalDate date = today.plusDays(i);
            dateOptions.add(date.toString()); // yyyy-MM-dd
        }
        JComboBox<String> dateBox = new JComboBox<>(dateOptions.toArray(new String[0]));

        // Список времени по 15 минут (07:00 - 21:45)
        List<String> timeOptions = new ArrayList<>();
        for (int hour = 7; hour <= 21; hour++) {
            for (int minute = 0; minute < 60; minute += 15) {
                timeOptions.add(String.format("%02d:%02d", hour, minute));
            }
        }
        JComboBox<String> timeBox = new JComboBox<>(timeOptions.toArray(new String[0]));

        add(new JLabel("Инструктор:"));
        add(instructorBox);

        add(new JLabel("Машина:"));
        add(carBox);

        add(new JLabel("Дата:"));
        add(dateBox);

        add(new JLabel("Время:"));
        add(timeBox);

        JButton saveButton = new JButton("Сохранить");
        saveButton.addActionListener(e -> {
            try {
                String instructor = ((String) instructorBox.getSelectedItem()).trim().toLowerCase();
                String car = ((String) carBox.getSelectedItem()).trim().toLowerCase();
                LocalDate date = LocalDate.parse((String) Objects.requireNonNull(dateBox.getSelectedItem()));
                LocalTime time = LocalTime.parse((String) Objects.requireNonNull(timeBox.getSelectedItem()));

                Window window = new Window();
                window.setInstructorName(instructor);
                window.setCarName(car);
                window.setDate(date);
                window.setTimeFrom(time);

                windowService.bookWindow(window);
                JOptionPane.showMessageDialog(this, "Окно добавлено успешно.");
                dispose();

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Ошибка при вводе данных:\n" + ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        });

        add(new JLabel());
        add(saveButton);
    }
}
