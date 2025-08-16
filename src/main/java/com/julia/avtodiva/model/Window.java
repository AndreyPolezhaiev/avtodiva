package com.julia.avtodiva.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "windows")
@Data
public class Window {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String instructorName;
    private String carName;
    private LocalTime timeFrom;
    private LocalTime timeTo;
    private LocalDate date;
}
