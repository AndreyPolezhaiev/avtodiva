package com.julia.avtodiva.service.schedule;

import com.julia.avtodiva.model.ScheduleSlot;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface ScheduleSlotService {
    List<ScheduleSlot> findAllSlots(List<String> instructorNames, List<String> carNames, int daysAhead);
    List<ScheduleSlot> findBookedSlots(List<String> instructorNames, List<String> carNames, int daysAhead);
    List<ScheduleSlot> findFreeSlots(List<String> instructorNames, List<String> carNames, int daysAhead);
    void updateSlot(ScheduleSlot slot);
    void saveAllSlots(List<ScheduleSlot> slots);
    public boolean rescheduleSlot(ScheduleSlot slot);
}
