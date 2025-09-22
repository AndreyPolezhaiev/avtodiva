package com.julia.avtodiva.ui.panel.dialog;

import com.julia.avtodiva.model.Instructor;
import com.julia.avtodiva.model.ScheduleSlot;
import com.julia.avtodiva.model.Weekend;
import com.julia.avtodiva.service.instructor.InstructorService;
import com.julia.avtodiva.service.schedule.ScheduleSlotService;
import com.julia.avtodiva.service.weekend.WeekendService;
import com.julia.avtodiva.ui.util.MultiDateChooserDialog;

import javax.swing.*;
import java.awt.*;
import java.time.*;
import java.util.List;
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
    private final ScheduleSlotService scheduleSlotService;

    private final ZoneId zone = ZoneId.systemDefault();

    private JButton dateChooserBtn;
    private List<LocalDate> selectedDates;

    private JSpinner fromSp;
    private JSpinner toSp;

    public AddWeekendDialog(Window parent,
                            Long instructorId,
                            InstructorService instructorService,
                            WeekendService weekendService, ScheduleSlotService scheduleSlotService) {
        super(parent, "Додати вихідний", ModalityType.APPLICATION_MODAL);
        this.instructorId = instructorId;
        this.instructorService = instructorService;
        this.weekendService = weekendService;
        this.scheduleSlotService = scheduleSlotService;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        buildUI();
        pack();
        setLocationRelativeTo(parent);
    }

    private void buildUI() {
        dateChooserBtn = new JButton("Оберіть дні...");
        dateChooserBtn.addActionListener(e -> {
            MultiDateChooserDialog dlg = new MultiDateChooserDialog(this);
            dlg.setVisible(true);
            selectedDates = dlg.getSelectedDates();
            if (selectedDates != null && !selectedDates.isEmpty()) {
                dateChooserBtn.setText("Вибрано: " + selectedDates.size());
            } else {
                dateChooserBtn.setText("Оберіть дні...");
            }
        });

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
        form.add(dateChooserBtn, c);
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
        if (selectedDates == null || selectedDates.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Оберіть хоча б одну дату!", "Помилка", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            LocalTime tFrom = ((Date) fromSp.getValue()).toInstant().atZone(zone).toLocalTime().withSecond(0).withNano(0);
            LocalTime tTo   = ((Date) toSp.getValue()).toInstant().atZone(zone).toLocalTime().withSecond(0).withNano(0);

            if (!tTo.isAfter(tFrom)) {
                JOptionPane.showMessageDialog(this, "«Час до» повинен бути після «Час з».", "Помилка", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Instructor inst = instructorService.findById(instructorId);

            // 1. Проверяем все даты на конфликты
            StringBuilder conflicts = new StringBuilder();
            for (LocalDate day : selectedDates) {
                List<ScheduleSlot> bookedSlots = scheduleSlotService.findAllBookedSlotsByInstructorName(inst.getName());

                boolean conflict = bookedSlots.stream().anyMatch(slot ->
                        selectedDates.contains(slot.getDate()) &&
                                !(slot.getTimeTo().isBefore(tFrom) || slot.getTimeFrom().isAfter(tTo))
                );

                if (conflict) {
                    conflicts.append(day).append(" має зайняті слоти!\n");
                }
            }

            if (!conflicts.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Не вдалося зберегти вихідні:\n" + conflicts,
                        "Конфлікт", JOptionPane.WARNING_MESSAGE);
                return; // ❌ прекращаем сохранение
            }

            // 2. Если конфликтов нет — сохраняем все даты
            for (LocalDate day : selectedDates) {
                Weekend w = new Weekend();
                w.setDay(day);
                w.setTimeFrom(tFrom);
                w.setTimeTo(tTo);
                w.setInstructor(inst);
                weekendService.save(w);
            }

            JOptionPane.showMessageDialog(this, "Вихідні додані",
                    "Успіх", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Не вдалося зберегти вихідний. Перевірте дані.", "Помилка", JOptionPane.ERROR_MESSAGE);
        }
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
