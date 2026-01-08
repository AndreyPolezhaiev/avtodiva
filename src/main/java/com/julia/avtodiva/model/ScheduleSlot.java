package com.julia.avtodiva.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "schedule_slots", indexes = {
        // 1. Поиск слотов инструктора за период (самый частый запрос)
        @Index(name = "idx_instructor_date", columnList = "instructor_id, date"),

        // 2. Поиск по студенту (findByStudentNameIgnoreCase)
        @Index(name = "idx_student_id", columnList = "student_id"),

        // 3. Поиск по автомобилю за период (для find*SlotsBetween)
        @Index(name = "idx_car_date", columnList = "car_id, date"),

        // 4. Поиск конфликтов по дате и времени
        @Index(name = "idx_date_time_from_to", columnList = "date, timeFrom, timeTo"),

        // 5. Быстрый поиск забронированных слотов по инструктору (findAllBookedSlotsByInstructorName)
        @Index(name = "idx_booked_instructor", columnList = "booked, instructor_id")
})
@Data
public class ScheduleSlot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate date;

    private LocalTime timeFrom;

    private LocalTime timeTo;
    @ToString.Exclude
    @ManyToOne
    private Instructor instructor;
    @ToString.Exclude
    @ManyToOne
    private Car car;
    @ToString.Exclude
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private Student student;

    private String description;

    private String link;

    private boolean booked;
}
