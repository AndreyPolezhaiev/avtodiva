// Instructor.java
package com.julia.avtodiva.model;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "instructors")
@Getter @Setter
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Instructor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @ToString.Include
    private Long id;

    @ToString.Include
    private String name;

    @OneToMany(
            mappedBy = "instructor",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.EAGER
    )
    @ToString.Exclude
    private List<Weekend> weekends = new ArrayList<>();

    @ToString.Exclude
    @OneToMany(mappedBy = "instructor",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<ScheduleSlot> slots = new ArrayList<>();
}
