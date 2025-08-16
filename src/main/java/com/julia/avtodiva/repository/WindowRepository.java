package com.julia.avtodiva.repository;

import com.julia.avtodiva.model.Window;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface WindowRepository extends JpaRepository<Window, Long> {
    List<Window> findByInstructorNameAndCarNameAndDateBetween(
            String instructorName, String carName, LocalDate start, LocalDate end);
}
