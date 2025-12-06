package com.julia.avtodiva.ui.panel.dialog;

import com.julia.avtodiva.service.window.WindowService;
import com.julia.avtodiva.service.instructor.InstructorService;

import javax.swing.*;
import java.awt.*;

public class AddWindowsForInstructorDialog extends JDialog {
    private final WindowService windowService;
    private final InstructorService instructorService;

    private JComboBox<String> instructorCombo;
    private JTextField daysField;

    public AddWindowsForInstructorDialog(Window parent, WindowService windowService, InstructorService instructorService) {
        super(parent, "Додати вільні місця для інструктора", ModalityType.APPLICATION_MODAL);
        this.windowService = windowService;
        this.instructorService = instructorService;

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        buildUI();
        pack();
        setLocationRelativeTo(parent);
    }

    private void buildUI() {
        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.anchor = GridBagConstraints.LINE_END;

        // выпадающий список с инструкторами
        gbc.gridx = 0; gbc.gridy = 0;
        form.add(new JLabel("Інструктор:"), gbc);
        gbc.gridx = 1;
        instructorCombo = new JComboBox<>(instructorService.getInstructorsNames());
        form.add(instructorCombo, gbc);

        // поле для количества дней
        gbc.gridx = 0; gbc.gridy = 1;
        form.add(new JLabel("Кількість днів:"), gbc);
        gbc.gridx = 1;
        daysField = new JTextField(10);
        form.add(daysField, gbc);

        // кнопки
        JButton ok = new JButton("Додати");
        JButton cancel = new JButton("Відхилити");

        ok.addActionListener(e -> onSave());
        cancel.addActionListener(e -> dispose());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(ok);
        buttons.add(cancel);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(form, BorderLayout.CENTER);
        getContentPane().add(buttons, BorderLayout.SOUTH);
    }

    private void onSave() {
        String instructorName = (String) instructorCombo.getSelectedItem();
        String daysStr = daysField.getText().trim();

        if (instructorName == null || instructorName.isBlank()) {
            JOptionPane.showMessageDialog(this, "Виберіть інструктора!", "Помилка", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            int days = Integer.parseInt(daysStr);
            if (days <= 0) throw new NumberFormatException();

            windowService.addFreeWindowsForInstructor(instructorName, days);

            JOptionPane.showMessageDialog(this,
                    "Вільні місця для інструктора '" + instructorName + "' додані!",
                    "Успіх", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "Введіть коректне число днів!",
                    "Помилка", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Не вдалося додати місця: " + ex.getMessage(),
                    "Помилка", JOptionPane.ERROR_MESSAGE);
        }
    }
}