package com.julia.avtodiva.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table (name = "schedule_slots")
@Data
public class ScheduleSlot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate date;

    private LocalTime timeFrom;

    private LocalTime timeTo;

    @ManyToOne
    private Instructor instructor;

    @ManyToOne
    private Car car;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private Student student;

    private String description;

    private String link;

    private boolean booked;
}
