package com.julia.avtodiva.repository;

import com.julia.avtodiva.model.Car;
import com.julia.avtodiva.model.Instructor;
import com.julia.avtodiva.model.ScheduleSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface ScheduleSlotRepository extends JpaRepository<ScheduleSlot, Long> {

    @Query("SELECT s FROM ScheduleSlot s WHERE s.booked = true " +
            "AND s.instructor.name = :instructorName AND s.car.name = :carName " +
            "AND s.date BETWEEN :start AND :end")
    List<ScheduleSlot> findBookedSlotsBetween(
            @Param("instructorName") String instructorName,
            @Param("carName") String carName,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end);

    @Query("SELECT s FROM ScheduleSlot s WHERE s.booked = false " +
            "AND s.instructor.name = :instructorName AND s.car.name = :carName " +
            "AND s.date BETWEEN :start AND :end")
    List<ScheduleSlot> findFreeSlotsBetween(
            @Param("instructorName") String instructorName,
            @Param("carName") String carName,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end);

    @Query("SELECT s FROM ScheduleSlot s WHERE s.instructor.name = :instructorName " +
            "AND s.car.name = :carName " +
            "AND s.date BETWEEN :start AND :end")
    List<ScheduleSlot> findAllSlots(
            @Param("instructorName") String instructorName,
            @Param("carName") String carName,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end);

    boolean existsByDateAndTimeFromAndInstructorAndCar(
            LocalDate date,
            LocalTime timeFrom,
            Instructor instructor,
            Car car
    );
}
