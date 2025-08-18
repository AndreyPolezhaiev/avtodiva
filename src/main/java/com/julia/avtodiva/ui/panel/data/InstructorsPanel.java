// src/main/java/com/julia/avtodiva/ui/panel/data/InstructorsPanel.java
package com.julia.avtodiva.ui.panel.data;

import com.julia.avtodiva.model.Instructor;
import com.julia.avtodiva.model.Weekend;
import com.julia.avtodiva.service.instructor.InstructorService;
import com.julia.avtodiva.service.weekend.WeekendService;
import com.julia.avtodiva.ui.MainFrame;
import com.julia.avtodiva.ui.model.PanelName;
import com.julia.avtodiva.ui.panel.data.table.InstructorWeekendsTableModel;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.*;
import java.util.List;

@Component
public class InstructorsPanel extends JPanel {
    private final MainFrame mainFrame;
    private final InstructorService instructorService;
    private final WeekendService weekendService;

    public InstructorsPanel(@Lazy MainFrame mainFrame, InstructorService instructorService, WeekendService weekendService) {
        this.mainFrame = mainFrame;
        this.instructorService = instructorService;
        this.weekendService = weekendService;
        setLayout(new BorderLayout());
    }

    public void refreshInstructor(Instructor instructor) {
        removeAll();

        final Long instructorId = instructor.getId();
        InstructorWeekendsTableModel tableModel = new InstructorWeekendsTableModel(instructor);
        JTable table = new JTable(tableModel);
        table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);
        add(createBottomPanel(tableModel, table, instructorId), BorderLayout.SOUTH);

        revalidate();
        repaint();
    }

    private JPanel createBottomPanel(InstructorWeekendsTableModel tableModel, JTable table, Long instructorId) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton addButton = getAddButton(table, instructorId);

        JButton deleteButton = getDeleteButton(tableModel, table, instructorId);

        JButton saveButton = getSaveButton(tableModel, table, instructorId);

        JButton backButton = new JButton("Назад");
        backButton.addActionListener(l -> mainFrame.showPanel(PanelName.RANGE_SELECTION_PANEL.name()));

        panel.add(addButton);
        panel.add(deleteButton);
        panel.add(saveButton);
        panel.add(backButton);
        return panel;
    }

    private JButton getSaveButton(InstructorWeekendsTableModel tableModel, JTable table, Long instructorId) {
        JButton saveButton = new JButton("Сохранить выбранные");
        saveButton.addActionListener(e -> {
            if (table.isEditing()) table.getCellEditor().stopCellEditing();
            List<Weekend> selected = tableModel.getSelectedWeekends();
            if (selected.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Нет выбранных выходных", "Предупреждение", JOptionPane.WARNING_MESSAGE);
                return;
            }
            weekendService.saveAllWeekends(selected);
            JOptionPane.showMessageDialog(this, "Выходные успешно сохранены", "Успех", JOptionPane.INFORMATION_MESSAGE);
            Instructor fresh = instructorService.findById(instructorId);
            refreshInstructor(fresh);
        });
        return saveButton;
    }

    private JButton getDeleteButton(InstructorWeekendsTableModel tableModel, JTable table, Long instructorId) {
        JButton deleteButton = new JButton("Удалить выбранный выходной");
        deleteButton.addActionListener(e -> {
            if (table.isEditing()) table.getCellEditor().stopCellEditing();
            List<Weekend> selected = tableModel.getSelectedWeekends();
            if (selected.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Нет выбранных выходных", "Предупреждение", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String msg = selected.size() == 1
                    ? "Удалить выбранный выходной?"
                    : "Удалить выбранные выходные (" + selected.size() + " шт.)?";
            int ans = JOptionPane.showConfirmDialog(this, msg, "Подтверждение", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (ans == JOptionPane.YES_OPTION) {
                weekendService.deleteAllWeekends(selected); // реализуй в сервисе удаление списка
                JOptionPane.showMessageDialog(this, "Удаление выполнено", "Готово", JOptionPane.INFORMATION_MESSAGE);
                Instructor fresh = instructorService.findById(instructorId);
                refreshInstructor(fresh);
            }
        });
        return deleteButton;
    }

    private JButton getAddButton(JTable table, Long instructorId) {
        JButton addButton = new JButton("Добавить выходной");
        addButton.addActionListener(e -> {
            if (table.isEditing()) table.getCellEditor().stopCellEditing();
            Weekend toAdd = showAddWeekendDialog(instructorId);
            if (toAdd == null) return;
            weekendService.saveAllWeekends(java.util.Collections.singletonList(toAdd));
            JOptionPane.showMessageDialog(this, "Выходной добавлен", "Успех", JOptionPane.INFORMATION_MESSAGE);
            Instructor fresh = instructorService.findById(instructorId);
            refreshInstructor(fresh);
        });
        return addButton;
    }

    /* ==== helpers ==== */
    private Weekend showAddWeekendDialog(Long instructorId) {
        // дефолты: сегодня, 09:00–11:00
        java.time.ZoneId zone = java.time.ZoneId.systemDefault();
        JSpinner dateSp  = createDateSpinner(java.time.LocalDate.now(), zone);
        JSpinner fromSp  = createTimeSpinner(java.time.LocalTime.of(9, 0), zone);
        JSpinner toSp    = createTimeSpinner(java.time.LocalTime.of(11, 0), zone);

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new java.awt.Insets(6, 8, 6, 8);
        c.gridx = 0; c.gridy = 0; c.anchor = GridBagConstraints.LINE_END;
        form.add(new JLabel("Дата:"), c);
        c.gridy = 1; form.add(new JLabel("Время с:"), c);
        c.gridy = 2; form.add(new JLabel("Время до:"), c);
        c.gridx = 1; c.gridy = 0; c.anchor = GridBagConstraints.LINE_START;
        form.add(dateSp, c);
        c.gridy = 1; form.add(fromSp, c);
        c.gridy = 2; form.add(toSp, c);

        int res = JOptionPane.showConfirmDialog(
                this, form, "Добавить выходной",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE
        );
        if (res != JOptionPane.OK_OPTION) return null;

        try {
            java.util.Date d  = (java.util.Date) dateSp.getValue();
            java.util.Date df = (java.util.Date) fromSp.getValue();
            java.util.Date dt = (java.util.Date) toSp.getValue();

            java.time.LocalDate day    = d.toInstant().atZone(zone).toLocalDate();
            java.time.LocalTime tFrom  = df.toInstant().atZone(zone).toLocalTime().withSecond(0).withNano(0);
            java.time.LocalTime tTo    = dt.toInstant().atZone(zone).toLocalTime().withSecond(0).withNano(0);

            if (!tTo.isAfter(tFrom)) {
                JOptionPane.showMessageDialog(this, "«Время до» должно быть позже «Время с».", "Ошибка", JOptionPane.ERROR_MESSAGE);
                return null;
            }

            Weekend w = new Weekend();
            w.setDay(day);
            w.setTimeFrom(tFrom);
            w.setTimeTo(tTo);
            Instructor inst = instructorService.findById(instructorId);
            w.setInstructor(inst);
            return w;
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Не удалось разобрать дату/время.", "Ошибка", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

    // Хелпер: спиннер даты с днём недели (локаль — рус)
    private JSpinner createDateSpinner(java.time.LocalDate value, java.time.ZoneId zone) {
        java.util.Date init = java.util.Date.from(value.atStartOfDay(zone).toInstant());
        javax.swing.SpinnerDateModel model = new javax.swing.SpinnerDateModel(init, null, null, java.util.Calendar.DAY_OF_MONTH);
        JSpinner sp = new JSpinner(model);
        JSpinner.DateEditor ed = new JSpinner.DateEditor(sp, "EEEE, dd.MM.yyyy");
        // русские названия дней недели
        java.text.DateFormatSymbols dfs = java.text.DateFormatSymbols.getInstance(new java.util.Locale("ru", "UA"));
        ed.getFormat().setDateFormatSymbols(dfs);
        sp.setEditor(ed);
        sp.setPreferredSize(new java.awt.Dimension(180, sp.getPreferredSize().height));
        attachMouseWheelSupport(sp);
        return sp;
    }

    private JSpinner createTimeSpinner(java.time.LocalTime value, java.time.ZoneId zone) {
        java.time.LocalDate today = java.time.LocalDate.now(zone);
        java.util.Date init = java.util.Date.from(value.atDate(today).atZone(zone).toInstant());
        javax.swing.SpinnerDateModel model = new javax.swing.SpinnerDateModel(init, null, null, java.util.Calendar.MINUTE);
        JSpinner sp = new JSpinner(model);
        sp.setEditor(new JSpinner.DateEditor(sp, "HH:mm"));
        sp.setPreferredSize(new java.awt.Dimension(80, sp.getPreferredSize().height));
        attachMouseWheelSupport(sp);
        return sp;
    }

    private void attachMouseWheelSupport(JSpinner spinner) {
        spinner.addMouseWheelListener(e -> {
            if (!spinner.isEnabled()) return;
            Object next = (e.getWheelRotation() < 0) ? spinner.getModel().getPreviousValue()
                    : spinner.getModel().getNextValue();
            if (next != null) spinner.getModel().setValue(next);
        });
    }

    private JFormattedTextField createMaskedField(String mask) {
        JFormattedTextField f = new JFormattedTextField();
        f.setFocusLostBehavior(JFormattedTextField.COMMIT);
        try {
            javax.swing.text.MaskFormatter mf = new javax.swing.text.MaskFormatter(mask);
            mf.setPlaceholderCharacter('_');
            f.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(mf));
        } catch (java.text.ParseException ignored) {}
        return f;
    }

}
