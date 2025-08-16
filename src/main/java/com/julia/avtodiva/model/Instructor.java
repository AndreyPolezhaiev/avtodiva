package com.julia.avtodiva.model;


import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Table (name = "instructors")
@Data
public class Instructor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<Weekend> weekends;
}
