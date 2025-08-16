package com.julia.avtodiva.service.schedule;

import com.julia.avtodiva.model.ScheduleSlot;
import com.julia.avtodiva.model.Student;
import com.julia.avtodiva.repository.ScheduleSlotRepository;
import com.julia.avtodiva.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ScheduleSlotServiceImpl implements ScheduleSlotService {
    private final ScheduleSlotRepository scheduleSlotRepository;
    private final StudentRepository studentRepository;

    @Override
    public List<ScheduleSlot> findAllSlots(String instructorName, String carName, int daysAhead) {
        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusDays(daysAhead);

        instructorName = instructorName == null ? "" : instructorName.toLowerCase();
        carName = carName == null ? "" : carName.toLowerCase();

        return scheduleSlotRepository.findAllSlots(instructorName, carName, today, endDate);
    }

    @Override
    public List<ScheduleSlot> findBookedSlots(String instructorName, String carName, int daysAhead) {
        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusDays(daysAhead);

        instructorName = instructorName == null ? "" : instructorName.toLowerCase();
        carName = carName == null ? "" : carName.toLowerCase();

        return scheduleSlotRepository.findBookedSlotsBetween(instructorName, carName, today, endDate);
    }

    @Override
    public List<ScheduleSlot> findFreeSlots(String instructorName, String carName, int daysAhead) {
        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusDays(daysAhead);

        instructorName = instructorName == null ? "" : instructorName.toLowerCase();
        carName = carName == null ? "" : carName.toLowerCase();

        return scheduleSlotRepository.findFreeSlotsBetween(instructorName, carName, today, endDate);
    }

    @Override
    public void updateSlot(ScheduleSlot slot) {
        scheduleSlotRepository.save(slot);
    }

    @Override
    public void saveAllSlots(List<ScheduleSlot> slots) {
        for (ScheduleSlot slot : slots) {
            Student student = slot.getStudent();
            if (student != null) {
                studentRepository.save(student);
            }
        }
        scheduleSlotRepository.saveAll(slots);
    }
}
