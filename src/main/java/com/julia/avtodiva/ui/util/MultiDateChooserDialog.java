package com.julia.avtodiva.ui.util;

import com.github.lgooddatepicker.components.CalendarPanel;
import com.github.lgooddatepicker.optionalusertools.CalendarListener;
import com.github.lgooddatepicker.zinternaltools.CalendarSelectionEvent;
import com.github.lgooddatepicker.zinternaltools.HighlightInformation;
import com.github.lgooddatepicker.zinternaltools.YearMonthChangeEvent;
import com.julia.avtodiva.ui.panel.renderer.LocalDateListRenderer;
import lombok.Getter;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class MultiDateChooserDialog extends JDialog {
    @Getter
    private final List<LocalDate> selectedDates = new ArrayList<>();
    private final DefaultListModel<LocalDate> listModel = new DefaultListModel<>();

    public MultiDateChooserDialog(Window parent) {
        super(parent, "Виберіть дні", ModalityType.APPLICATION_MODAL);
        setLayout(new BorderLayout());

        // сам календарь
        CalendarPanel calendarPanel = new CalendarPanel();
        // после создания calendarPanel
        calendarPanel.getSettings().setHighlightPolicy(date -> {
            if (selectedDates.contains(date)) {
                return new HighlightInformation(Color.GREEN, null, "Вибрано");
            }
            return null;
        });

        // список выбранных дат
        JList<LocalDate> list = new JList<>(listModel);
        list.setCellRenderer(new LocalDateListRenderer());
        // обработчик клика по дню
        calendarPanel.addCalendarListener(new CalendarListener() {
            @Override
            public void selectedDateChanged(CalendarSelectionEvent event) {
                LocalDate clickedDate = event.getNewDate();
                if (clickedDate != null) {
                    if (selectedDates.contains(clickedDate)) {
                        selectedDates.remove(clickedDate);
                        listModel.removeElement(clickedDate);
                    } else {
                        selectedDates.add(clickedDate);
                        listModel.addElement(clickedDate);
                    }
                    calendarPanel.repaint();
                }
            }

            @Override
            public void yearMonthChanged(YearMonthChangeEvent event) {}
        });

        add(calendarPanel, BorderLayout.NORTH);
        add(new JScrollPane(list), BorderLayout.CENTER);

        // нижняя панель
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton okBtn = new JButton("OK");
        okBtn.addActionListener(e -> dispose());
        JButton cancelBtn = new JButton("Відхилити");
        cancelBtn.addActionListener(e -> {
            selectedDates.clear();
            dispose();
        });

        bottom.add(okBtn);
        bottom.add(cancelBtn);

        add(bottom, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(parent);
    }
}
