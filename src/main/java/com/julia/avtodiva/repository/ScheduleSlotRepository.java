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
import java.util.Optional;

@Repository
public interface ScheduleSlotRepository extends JpaRepository<ScheduleSlot, Long> {

    @Query("""
            SELECT s FROM ScheduleSlot s
            WHERE s.booked = true
              AND LOWER(s.instructor.name) IN :instructorNames
              AND LOWER(s.car.name) IN :carNames
              AND s.date BETWEEN :start AND :end
              AND NOT EXISTS (
                  SELECT w FROM Weekend w
                  WHERE w.instructor = s.instructor
                    AND w.day = s.date
                    AND s.timeFrom < w.timeTo
                    AND s.timeTo > w.timeFrom
              )
            """)
    List<ScheduleSlot> findBookedSlotsBetween(
            @Param("instructorNames") List<String> instructorNames,
            @Param("carNames") List<String> carNames,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end);


    @Query("""
            SELECT s FROM ScheduleSlot s
            WHERE s.booked = false
              AND LOWER(s.instructor.name) IN :instructorNames
              AND LOWER(s.car.name) IN :carNames
              AND s.date BETWEEN :start AND :end
              AND NOT EXISTS (
                  SELECT w FROM Weekend w
                  WHERE w.instructor = s.instructor
                    AND w.day = s.date
                    AND s.timeFrom < w.timeTo
                    AND s.timeTo > w.timeFrom
              )
              AND NOT EXISTS (
                  SELECT s2 FROM ScheduleSlot s2
                  WHERE s2.car = s.car
                    AND s2.date = s.date
                    AND s2.booked = true
                    AND s2.timeFrom < s.timeTo
                    AND s2.timeTo > s.timeFrom
              )
            """)
    List<ScheduleSlot> findFreeSlotsBetween(
            @Param("instructorNames") List<String> instructorNames,
            @Param("carNames") List<String> carNames,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end);


    @Query("""
            SELECT s FROM ScheduleSlot s
            WHERE LOWER(s.instructor.name) IN :instructorNames
              AND LOWER(s.car.name) IN :carNames
              AND s.date BETWEEN :start AND :end
              AND NOT EXISTS (
                  SELECT w FROM Weekend w
                  WHERE w.instructor = s.instructor
                    AND w.day = s.date
                    AND s.timeFrom < w.timeTo
                    AND s.timeTo > w.timeFrom
              )
            """)
    List<ScheduleSlot> findAllSlots(
            @Param("instructorNames") List<String> instructorNames,
            @Param("carNames") List<String> carNames,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end);


    boolean existsByDateAndTimeFromAndInstructorAndCar(
            LocalDate date,
            LocalTime timeFrom,
            Instructor instructor,
            Car car
    );

    @Query("SELECT s FROM ScheduleSlot s " +
            "WHERE LOWER(s.instructor.name) = LOWER(:instructor) " +
            "AND LOWER(s.car.name) = LOWER(:car) " +
            "AND s.date = :date " +
            "AND s.timeFrom = :timeFrom")
    Optional<ScheduleSlot> findByInstructorCarDateTime(
            @Param("instructor") String instructor,
            @Param("car") String car,
            @Param("date") LocalDate date,
            @Param("timeFrom") LocalTime timeFrom
    );
}
