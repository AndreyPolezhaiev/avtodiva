package com.julia.avtodiva.ui.panel.dialog;

import com.julia.avtodiva.model.Instructor;
import com.julia.avtodiva.service.instructor.InstructorService;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class AddInstructorDialog extends JDialog {
    private final InstructorService instructorService;

    private JComboBox<String> actionCombo;
    private JTextField nameField;
    private JComboBox<String> existingInstructors;

    public AddInstructorDialog(Window parent, InstructorService instructorService) {
        super(parent, "Керування інструкторами", ModalityType.APPLICATION_MODAL);
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

        // label "Дія"
        gbc.gridx = 0;
        gbc.gridy = 0;
        form.add(new JLabel("Дія:"), gbc);

        // выпадающий список действий
        gbc.gridx = 1;
        actionCombo = new JComboBox<>(new String[]{"Додати", "Видалити"});
        form.add(actionCombo, gbc);

        // поле ввода для "Додати"
        gbc.gridy = 1;
        gbc.gridx = 0;
        form.add(new JLabel("Ім'я:"), gbc);

        gbc.gridx = 1;
        nameField = new JTextField(20);
        form.add(nameField, gbc);

        // выпадающий список для "Видалити"
        existingInstructors = new JComboBox<>();
        existingInstructors.setVisible(false); // по умолчанию скрыт
        form.add(existingInstructors, gbc);

        // слушатель переключения
        actionCombo.addActionListener(e -> toggleMode());

        // кнопки
        JButton ok = new JButton("Підтвердити");
        JButton cancel = new JButton("Відхилити");

        ok.addActionListener(e -> onSave());
        cancel.addActionListener(e -> dispose());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(ok);
        buttons.add(cancel);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(form, BorderLayout.CENTER);
        getContentPane().add(buttons, BorderLayout.SOUTH);

        toggleMode(); // выставляем начальный режим
    }

    private void toggleMode() {
        String selected = (String) actionCombo.getSelectedItem();
        if ("Додати".equals(selected)) {
            nameField.setVisible(true);
            existingInstructors.setVisible(false);
        } else {
            nameField.setVisible(false);
            existingInstructors.setVisible(true);

            // обновляем список
            existingInstructors.removeAllItems();
            List<String> instructors = List.of(instructorService.getInstructorsNames());
            for (String i : instructors) {
                existingInstructors.addItem(i);
            }
        }
        revalidate();
        repaint();
    }

    private void onSave() {
        String action = (String) actionCombo.getSelectedItem();

        if ("Додати".equals(action)) {
            String name = nameField.getText().trim();
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Ім'я інструктора не може бути порожнім!",
                        "Помилка", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                Instructor instructor = new Instructor();
                instructor.setName(name);
                instructorService.saveInstructor(instructor);

                JOptionPane.showMessageDialog(this,
                        "Інструктор доданий успішно!",
                        "Успіх", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        ex.getMessage(),
                        "Помилка", JOptionPane.ERROR_MESSAGE);
            }
        } else if ("Видалити".equals(action)) {
            String selected = (String) existingInstructors.getSelectedItem();
            if (selected == null) {
                JOptionPane.showMessageDialog(this,
                        "Виберіть інструктора для видалення!",
                        "Помилка", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                instructorService.deleteByName(selected);
                JOptionPane.showMessageDialog(this,
                        "Інструктора '" + selected + "' видалено!",
                        "Успіх", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Не вдалося видалити інструктора: " + ex.getMessage(),
                        "Помилка", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
