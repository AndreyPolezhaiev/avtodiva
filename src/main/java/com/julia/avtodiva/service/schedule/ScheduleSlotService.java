package com.julia.avtodiva.service.schedule;

import com.julia.avtodiva.model.ScheduleSlot;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface ScheduleSlotService {
    List<ScheduleSlot> findAllSlots(List<String> instructorNames, List<String> carNames, LocalDate start, LocalDate end);
    List<ScheduleSlot> findBookedSlots(List<String> instructorNames, List<String> carNames, LocalDate start, LocalDate end);
    List<ScheduleSlot> findFreeSlots(List<String> instructorNames, List<String> carNames, LocalDate start, LocalDate end);
    void updateSlot(ScheduleSlot slot);
    public boolean rescheduleSlot(ScheduleSlot slot);
    List<ScheduleSlot> findByInstructorAndStudentNames(String instructorName, String studentName);
    List<ScheduleSlot> findByInstructorName(String instructorName);
    List<ScheduleSlot> findByStudentName(String studentName);
    List<ScheduleSlot> filterSlotsByTime(List<ScheduleSlot> slots, List<String> selectedTimes);
    List<ScheduleSlot> findAllBookedSlotsByInstructorName(String instructorName);
    void createSlot(ScheduleSlot newSlot);
}
