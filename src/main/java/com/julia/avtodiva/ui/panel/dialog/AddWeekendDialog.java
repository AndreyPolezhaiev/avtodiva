package com.julia.avtodiva.ui.panel.dialog;

import com.julia.avtodiva.model.Instructor;
import com.julia.avtodiva.model.Weekend;
import com.julia.avtodiva.service.instructor.InstructorService;
import com.julia.avtodiva.service.weekend.WeekendService;

import javax.swing.*;
import java.awt.*;
import java.time.*;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;

/**
 * Модальный диалог "Добавить выходной".
 * Сам создает Weekend и сохраняет его через WeekendService.
 * Пример вызова:
 *   new AddWeekendDialog(parent, instructorId, instructorService, weekendService).setVisible(true);
 */
public class AddWeekendDialog extends JDialog {
    private final Long instructorId;
    private final InstructorService instructorService;
    private final WeekendService weekendService;

    private final ZoneId zone = ZoneId.systemDefault();

    private JSpinner dateSp;
    private JSpinner fromSp;
    private JSpinner toSp;

    public AddWeekendDialog(Window parent,
                            Long instructorId,
                            InstructorService instructorService,
                            WeekendService weekendService) {
        super(parent, "Додати вихідний", ModalityType.APPLICATION_MODAL);
        this.instructorId = instructorId;
        this.instructorService = instructorService;
        this.weekendService = weekendService;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        buildUI();
        pack();
        setLocationRelativeTo(parent);
    }

    private void buildUI() {
        dateSp = createDateSpinner(LocalDate.now(), zone);
        fromSp = createTimeSpinner(LocalTime.of(7, 0), zone);
        toSp   = createTimeSpinner(LocalTime.of(19, 0), zone);

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 8, 6, 8);
        c.gridx = 0; c.gridy = 0; c.anchor = GridBagConstraints.LINE_END;
        form.add(new JLabel("Дата:"), c);
        c.gridy = 1; form.add(new JLabel("Час з:"), c);
        c.gridy = 2; form.add(new JLabel("Час до:"), c);

        c.gridx = 1; c.gridy = 0; c.anchor = GridBagConstraints.LINE_START;
        form.add(dateSp, c);
        c.gridy = 1; form.add(fromSp, c);
        c.gridy = 2; form.add(toSp, c);

        JButton ok = new JButton("Додати");
        JButton cancel = new JButton("Відхилити");
        ok.addActionListener(e -> onSave());
        cancel.addActionListener(e -> dispose());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(ok); buttons.add(cancel);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(form, BorderLayout.CENTER);
        getContentPane().add(buttons, BorderLayout.SOUTH);
    }

    private void onSave() {
        try {
            Date d  = (Date) dateSp.getValue();
            Date df = (Date) fromSp.getValue();
            Date dt = (Date) toSp.getValue();

            LocalDate day   = d.toInstant().atZone(zone).toLocalDate();
            LocalTime tFrom = df.toInstant().atZone(zone).toLocalTime().withSecond(0).withNano(0);
            LocalTime tTo   = dt.toInstant().atZone(zone).toLocalTime().withSecond(0).withNano(0);

            if (!tTo.isAfter(tFrom)) {
                JOptionPane.showMessageDialog(this, "«Час до» повинен бути після «Час з».", "Помилка", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Instructor inst = instructorService.findById(instructorId);
            Weekend w = new Weekend();
            w.setDay(day);
            w.setTimeFrom(tFrom);
            w.setTimeTo(tTo);
            w.setInstructor(inst);

            weekendService.saveAllWeekends(Collections.singletonList(w));
            JOptionPane.showMessageDialog(this, "Вихідний доданий", "Успіх", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Не вдалося зберегти вихідний. Перевірте дані.", "Помилка", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ===== helpers =====
    private static JSpinner createDateSpinner(LocalDate value, ZoneId zone) {
        Date init = Date.from(value.atStartOfDay(zone).toInstant());
        SpinnerDateModel model = new SpinnerDateModel(init, null, null, java.util.Calendar.DAY_OF_MONTH);
        JSpinner sp = new JSpinner(model);
        JSpinner.DateEditor ed = new JSpinner.DateEditor(sp, "EEEE, dd.MM.yyyy");
        java.text.DateFormatSymbols dfs = java.text.DateFormatSymbols.getInstance(new Locale("uk", "UA"));
        ed.getFormat().setDateFormatSymbols(dfs);
        sp.setEditor(ed);
        sp.setPreferredSize(new Dimension(200, sp.getPreferredSize().height));
        attachMouseWheelSupport(sp);
        return sp;
    }

    private static JSpinner createTimeSpinner(LocalTime value, ZoneId zone) {
        LocalDate today = LocalDate.now(zone);
        Date init = Date.from(value.atDate(today).atZone(zone).toInstant());
        SpinnerDateModel model = new SpinnerDateModel(init, null, null, java.util.Calendar.MINUTE);
        JSpinner sp = new JSpinner(model);
        sp.setEditor(new JSpinner.DateEditor(sp, "HH:mm"));
        sp.setPreferredSize(new Dimension(90, sp.getPreferredSize().height));
        attachMouseWheelSupport(sp);
        return sp;
    }

    private static void attachMouseWheelSupport(JSpinner spinner) {
        spinner.addMouseWheelListener(e -> {
            if (!spinner.isEnabled()) return;
            Object next = (e.getWheelRotation() < 0) ? spinner.getModel().getPreviousValue()
                    : spinner.getModel().getNextValue();
            if (next != null) spinner.getModel().setValue(next);
        });
    }
}
