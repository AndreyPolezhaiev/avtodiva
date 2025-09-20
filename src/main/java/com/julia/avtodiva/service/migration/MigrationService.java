package com.julia.avtodiva.service.migration;

import com.julia.avtodiva.model.ScheduleSlot;
import com.julia.avtodiva.repository.ScheduleSlotRepository;
import com.julia.avtodiva.service.window.WorkingHoursProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MigrationService {
    private final ScheduleSlotRepository scheduleSlotRepository;

    @Transactional
    public void migrateSlotsToNewRules() {
        List<ScheduleSlot> allSlots = scheduleSlotRepository.findAll();

        for (ScheduleSlot slot : allSlots) {
            String instructor = slot.getInstructor().getName();
            LocalDate date = slot.getDate();
            LocalTime from = slot.getTimeFrom();

            // 1. Удалить все записи 18:30 у всех кроме Діна и Юлія
            if (from.equals(LocalTime.of(18, 30))
                    && !(instructor.equalsIgnoreCase("Діна") || instructor.equalsIgnoreCase("Юлія"))) {
                scheduleSlotRepository.delete(slot);
                continue;
            }

            // 2. Для Діна
            if (instructor.equalsIgnoreCase("Діна")) {
                // суббота 18:30 → удалить
                if (date.getDayOfWeek() == DayOfWeek.SATURDAY && from.equals(LocalTime.of(18, 30))) {
                    scheduleSlotRepository.delete(slot);
                    continue;
                }

                // суббота 15:00 → перенести на 16:00
                if (date.getDayOfWeek() == DayOfWeek.SATURDAY && from.equals(LocalTime.of(15, 0))) {
                    slot.setTimeFrom(LocalTime.of(16, 0));
                    slot.setTimeTo(LocalTime.of(19, 0)); // у Діни субботнее занятие длится 3 часа
                    scheduleSlotRepository.save(slot);
                    continue;
                }

                // любое 18:30 → поменять на 18:15
                if (from.equals(LocalTime.of(18, 30))) {
                    slot.setTimeFrom(LocalTime.of(18, 15));
                    slot.setTimeTo(LocalTime.of(20, 15)); // у Діни вечерние по 2 часа
                    scheduleSlotRepository.save(slot);
                    continue;
                }
            }

            // 3. Для Юлія
            if (instructor.equalsIgnoreCase("Юлія")) {
                if (from.equals(LocalTime.of(18, 30))) {
                    slot.setTimeFrom(LocalTime.of(18, 15));
                    slot.setTimeTo(LocalTime.of(20, 15)); // у Юлії вечерние по 2 часа
                    scheduleSlotRepository.save(slot);
                }
            }
        }
    }


}
