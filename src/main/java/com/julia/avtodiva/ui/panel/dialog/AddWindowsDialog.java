package com.julia.avtodiva.ui.panel.dialog;

import com.julia.avtodiva.service.window.WindowService;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class AddWindowsDialog extends JDialog {

    private final JTextField daysField = new JTextField();

    public AddWindowsDialog(JFrame parent, WindowService windowService) {
        super(parent, "Додати вільні вікна", true);
        setSize(400, 200);
        setLocationRelativeTo(parent);
        setLayout(new GridLayout(2, 2, 10, 10));

        List<Integer> dayOptions = new ArrayList<>();
        for(int i = 0; i <= 100; i++) {
            dayOptions.add(i);
        }
        JComboBox<Integer> daysBox = new JComboBox<>(dayOptions.toArray(new Integer[0]));

        add(new JLabel("Кількість днів:"));
        add(daysBox);

        JButton saveButton = new JButton("Додати");
        add(new JLabel());
        add(saveButton);

        saveButton.addActionListener(e -> {
            try {
                int days = ((Integer) daysBox.getSelectedItem()).intValue();

                windowService.addFreeWindowsForEachInstructor(days);

                dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Введіть коректне позитивне число днів.", "Помилка вводу", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
