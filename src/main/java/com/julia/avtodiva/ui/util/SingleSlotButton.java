package com.julia.avtodiva.ui.util;

import com.julia.avtodiva.service.car.CarService;
import com.julia.avtodiva.service.instructor.InstructorService;
import com.julia.avtodiva.service.schedule.ScheduleSlotService;
import com.julia.avtodiva.service.student.StudentService;
import com.julia.avtodiva.ui.panel.dialog.AddSingleSlotDialog;

import javax.swing.*;
import java.awt.*;

public class SingleSlotButton extends JButton {

    public SingleSlotButton(String text, JFrame owner,
                            ScheduleSlotService scheduleSlotService,
                            InstructorService instructorService,
                            CarService carService,
                            StudentService studentService) {
        super(text);

        this.setBackground(Color.decode("#bcdff7"));

        this.addActionListener(e -> {
            AddSingleSlotDialog dialog = new AddSingleSlotDialog(
                    owner,
                    scheduleSlotService,
                    instructorService,
                    carService,
                    studentService
            );
            dialog.setVisible(true);
        });
    }
}