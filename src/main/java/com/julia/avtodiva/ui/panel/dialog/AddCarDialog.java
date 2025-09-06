package com.julia.avtodiva.ui.panel.dialog;

import com.julia.avtodiva.model.Car;
import com.julia.avtodiva.service.car.CarService;

import javax.swing.*;
import java.awt.*;

public class AddCarDialog extends JDialog {
    private final CarService carService;

    private JComboBox<String> actionBox;
    private JTextField nameField;
    private JComboBox<String> existingCarsBox;

    public AddCarDialog(Window parent, CarService carService) {
        super(parent, "Керування машинами", ModalityType.APPLICATION_MODAL);
        this.carService = carService;
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

        // Выпадающий список: Додати / Видалити
        gbc.gridx = 0;
        gbc.gridy = 0;
        form.add(new JLabel("Опція:"), gbc);

        gbc.gridx = 1;
        actionBox = new JComboBox<>(new String[]{"Додати", "Видалити"});
        form.add(actionBox, gbc);

        // Поле ввода для имени машины
        gbc.gridy = 1;
        gbc.gridx = 0;
        form.add(new JLabel("Назва машини:"), gbc);

        gbc.gridx = 1;
        nameField = new JTextField(20);
        form.add(nameField, gbc);

        // Список существующих машин
        existingCarsBox = new JComboBox<>(carService.getCarsNames());
        existingCarsBox.setVisible(false); // по умолчанию скрыт
        gbc.gridx = 1;
        form.add(existingCarsBox, gbc);

        // Переключение между полем ввода и выпадающим списком
        actionBox.addActionListener(e -> toggleFields());

        // Кнопки
        JButton ok = new JButton("Підтвердити");
        JButton cancel = new JButton("Відмінити");

        ok.addActionListener(e -> onSave());
        cancel.addActionListener(e -> dispose());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(ok);
        buttons.add(cancel);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(form, BorderLayout.CENTER);
        getContentPane().add(buttons, BorderLayout.SOUTH);
    }

    private void toggleFields() {
        boolean isAdd = actionBox.getSelectedItem().equals("Додати");
        nameField.setVisible(isAdd);
        existingCarsBox.setVisible(!isAdd);
        revalidate();
        repaint();
    }

    private void onSave() {
        try {
            if (actionBox.getSelectedItem().equals("Додати")) {
                String name = nameField.getText().trim();
                if (name.isEmpty()) {
                    JOptionPane.showMessageDialog(this,
                            "Назва машини не може бути порожньою!",
                            "Помилка", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                Car car = new Car();
                car.setName(name);
                carService.saveCar(car);

                JOptionPane.showMessageDialog(this,
                        "Машину успішно додано!",
                        "Успіх", JOptionPane.INFORMATION_MESSAGE);
            } else {
                String name = (String) existingCarsBox.getSelectedItem();
                if (name == null) {
                    JOptionPane.showMessageDialog(this,
                            "Оберіть машину для видалення!",
                            "Помилка", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                carService.deleteByName(name);

                JOptionPane.showMessageDialog(this,
                        "Машину '" + name + "' видалено!",
                        "Успіх", JOptionPane.INFORMATION_MESSAGE);
            }

            dispose();
        } catch (IllegalStateException | IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this,
                    ex.getMessage(),
                    "Помилка", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Не вдалося виконати операцію з машиною.",
                    "Помилка", JOptionPane.ERROR_MESSAGE);
        }
    }
}
