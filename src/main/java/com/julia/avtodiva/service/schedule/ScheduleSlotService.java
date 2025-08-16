package com.julia.avtodiva.service.schedule;

import com.julia.avtodiva.model.ScheduleSlot;

import java.util.List;

public interface ScheduleSlotService {
    List<ScheduleSlot> findAllSlots(String instructorName, String carName, int daysAhead);
    List<ScheduleSlot> findBookedSlots(String instructorName, String carName, int daysAhead);
    List<ScheduleSlot> findFreeSlots(String instructorName, String carName, int daysAhead);
    void updateSlot(ScheduleSlot slot);
    void saveAllSlots(List<ScheduleSlot> slots);
}
