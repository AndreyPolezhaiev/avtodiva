package com.julia.avtodiva.ui.panel.dialog;

import com.julia.avtodiva.model.ScheduleSlot;
import com.julia.avtodiva.ui.panel.data.table.AllSlotsTableModel;

import javax.swing.*;
import java.awt.*;

public class SlotDetailsDialog extends JDialog {
    private final boolean editable;
    private final int row;
    private final AllSlotsTableModel tableModel;
    private final ScheduleSlot slot;

    private JTextField studentField;
    private JTextArea descArea;
    private JTextArea linkArea;

    public SlotDetailsDialog(Window parent, AllSlotsTableModel tableModel, ScheduleSlot slot, int row, boolean editable) {
        super(parent, "Деталі слота", ModalityType.APPLICATION_MODAL);
        this.tableModel = tableModel;
        this.slot = slot;
        this.row = row;
        this.editable = editable;

        buildUI();
        pack();
        setLocationRelativeTo(parent);
    }

    private void buildUI() {
        String currentStudent = (String) tableModel.getValueAt(row, 6);
        String currentDesc = (String) tableModel.getValueAt(row, 7);
        String currentLink = (String) tableModel.getValueAt(row, 8);

        studentField = new JTextField(currentStudent != null ? currentStudent : "");
        studentField.setPreferredSize(new Dimension(300, 25));

        descArea = new JTextArea(currentDesc != null ? currentDesc : "", 4, 30);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);

        linkArea = new JTextArea(currentLink != null ? currentLink : "", 2, 30);
        linkArea.setLineWrap(true);
        linkArea.setWrapStyleWord(true);

        if (!editable) {
            studentField.setEditable(false);
            descArea.setEditable(false);
            linkArea.setEditable(false);
        }

        JScrollPane descScroll = new JScrollPane(descArea);
        JScrollPane linkScroll = new JScrollPane(linkArea);

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;

        form.add(new JLabel("Дата: " + slot.getDate()), gbc);
        gbc.gridy++;
        form.add(new JLabel("Інструктор: " + slot.getInstructor().getName()), gbc);
        gbc.gridy++;
        form.add(new JLabel("Машина: " + slot.getCar().getName()), gbc);
        gbc.gridy++;
        form.add(new JLabel("Час: " + slot.getTimeFrom() + " - " + slot.getTimeTo()), gbc);

        gbc.gridwidth = 1;
        gbc.gridy++;
        gbc.gridx = 0;
        form.add(new JLabel("Учениця:"), gbc);
        gbc.gridx = 1;
        form.add(studentField, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        form.add(new JLabel("Опис:"), gbc);
        gbc.gridx = 1;
        form.add(descScroll, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        form.add(new JLabel("Посилання:"), gbc);
        gbc.gridx = 1;
        form.add(linkScroll, gbc);

        JPanel buttons = getJPanel();

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(form, BorderLayout.CENTER);
        getContentPane().add(buttons, BorderLayout.SOUTH);
    }

    private JPanel getJPanel() {
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        if (editable) {
            JButton saveBtn = new JButton("Зберегти");
            saveBtn.addActionListener(e -> onSave());
            JButton tgBtn = new JButton("Відправити в Telegram");
            tgBtn.addActionListener(e -> JOptionPane.showMessageDialog(this,
                    "Функціонал відправки у Telegram ще не реалізовано",
                    "Інформація", JOptionPane.INFORMATION_MESSAGE));
            JButton cancelBtn = new JButton("Відмінити");
            cancelBtn.addActionListener(e -> dispose());

            buttons.add(saveBtn);
            buttons.add(tgBtn);
            buttons.add(cancelBtn);
        } else {
            JButton closeBtn = new JButton("Закрити");
            closeBtn.addActionListener(e -> dispose());
            buttons.add(closeBtn);
        }
        return buttons;
    }

    private void onSave() {
        tableModel.setValueAt(studentField.getText(), row, 6);
        tableModel.setValueAt(descArea.getText(), row, 7);
        tableModel.setValueAt(linkArea.getText(), row, 8);
        dispose();
    }
}
