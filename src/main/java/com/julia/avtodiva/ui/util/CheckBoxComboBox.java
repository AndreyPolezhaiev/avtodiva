package com.julia.avtodiva.ui.util;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class CheckBoxComboBox extends JComboBox<JCheckBox> {
    private final List<String> selectedValues;

    public CheckBoxComboBox(String[] items, List<String> selectedValues) {
        super();
        this.selectedValues = selectedValues != null ? selectedValues : new ArrayList<>();

        for (String item : items) {
            JCheckBox cb = new JCheckBox(item);
            if (this.selectedValues.contains(item)) {
                cb.setSelected(true);
            }
            this.addItem(cb);
        }

        // рендерер для отображения
        this.setRenderer(new ListCellRenderer<JCheckBox>() {
            @Override
            public Component getListCellRendererComponent(JList<? extends JCheckBox> list,
                                                          JCheckBox value,
                                                          int index,
                                                          boolean isSelected,
                                                          boolean cellHasFocus) {
                if (index == -1 && value != null) {
                    String labelText = selectedValues.isEmpty()
                            ? "Оберіть час"
                            : (selectedValues.size() > 3
                                ? "Вибрано " + selectedValues.size()
                                : String.join(", ", selectedValues));

                    JLabel label = new JLabel(labelText);
                    label.setToolTipText(String.join(", ", selectedValues));
                    return label;
                }
                JCheckBox cb = value;
                cb.setBackground(isSelected ? list.getSelectionBackground() : list.getBackground());
                cb.setForeground(isSelected ? list.getSelectionForeground() : list.getForeground());
                return cb;
            }
        });

        // обработчик выбора
        this.addActionListener(e -> {
            Object selected = getSelectedItem();
            if (selected instanceof JCheckBox cb) {
                cb.setSelected(!cb.isSelected());
                if (cb.isSelected()) {
                    if (!selectedValues.contains(cb.getText())) {
                        selectedValues.add(cb.getText());
                    }
                } else {
                    selectedValues.remove(cb.getText());
                }
            }
            SwingUtilities.invokeLater(this::showPopup); // оставляем список открытым
        });
    }

    public List<String> getSelectedValues() {
        return new ArrayList<>(selectedValues);
    }
}
