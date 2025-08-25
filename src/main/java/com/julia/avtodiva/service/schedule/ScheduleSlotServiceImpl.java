package com.julia.avtodiva.service.schedule;

import com.julia.avtodiva.model.ScheduleSlot;
import com.julia.avtodiva.model.Student;
import com.julia.avtodiva.repository.ScheduleSlotRepository;
import com.julia.avtodiva.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ScheduleSlotServiceImpl implements ScheduleSlotService {
    private final ScheduleSlotRepository scheduleSlotRepository;
    private final StudentRepository studentRepository;

    @Override
    public List<ScheduleSlot> findAllSlots(List<String> instructorNames, List<String> carNames, int daysAhead) {
        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusDays(daysAhead);

        List<String> instructors = instructorNames == null ? List.of() :
                instructorNames.stream().map(String::toLowerCase).toList();
        List<String> cars = carNames == null ? List.of() :
                carNames.stream().map(String::toLowerCase).toList();

        return scheduleSlotRepository.findAllSlots(instructors, cars, today, endDate);
    }

    @Override
    public List<ScheduleSlot> findBookedSlots(List<String> instructorNames, List<String> carNames, int daysAhead) {
        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusDays(daysAhead);

        List<String> instructors = instructorNames == null ? List.of() :
                instructorNames.stream().map(String::toLowerCase).toList();
        List<String> cars = carNames == null ? List.of() :
                carNames.stream().map(String::toLowerCase).toList();

        return scheduleSlotRepository.findBookedSlotsBetween(instructors, cars, today, endDate);
    }

    @Override
    public List<ScheduleSlot> findFreeSlots(List<String> instructorNames, List<String> carNames, int daysAhead) {
        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusDays(daysAhead);

        List<String> instructors = instructorNames == null ? List.of() :
                instructorNames.stream().map(String::toLowerCase).toList();
        List<String> cars = carNames == null ? List.of() :
                carNames.stream().map(String::toLowerCase).toList();

        return scheduleSlotRepository.findFreeSlotsBetween(instructors, cars, today, endDate);
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

    @Override
    @Transactional
    public boolean rescheduleSlot(ScheduleSlot oldSlot, ScheduleSlot newSlot) {
        // Если ключевые поля не изменились — просто обновляем данные
        if (oldSlot.getDate().equals(newSlot.getDate()) &&
                oldSlot.getTimeFrom().equals(newSlot.getTimeFrom()) &&
                oldSlot.getInstructor().equals(newSlot.getInstructor()) &&
                oldSlot.getCar().equals(newSlot.getCar())) {

            oldSlot.setTimeTo(newSlot.getTimeTo());
            oldSlot.setStudent(newSlot.getStudent());
            oldSlot.setBooked(newSlot.isBooked());
            scheduleSlotRepository.save(oldSlot);
            return true;
        }

        // Проверяем, есть ли уже слот с новой комбинацией
        ScheduleSlot existing = scheduleSlotRepository.findByDateAndTimeFromAndInstructorAndCar(
                newSlot.getDate(),
                newSlot.getTimeFrom(),
                newSlot.getInstructor(),
                newSlot.getCar()
        );

        if (existing != null) {
            if (existing.isBooked()) {
                throw new IllegalStateException("Слот вже зайнятий іншим учнем!");
            }

            // Освободить старый
            oldSlot.setBooked(false);
            oldSlot.setStudent(null);
            scheduleSlotRepository.save(oldSlot);

            // Занять существующий
            existing.setBooked(true);
            existing.setStudent(newSlot.getStudent());
            existing.setTimeTo(newSlot.getTimeTo());
            scheduleSlotRepository.save(existing);
        } else {
            // Обновляем старый слот, а не создаём новый
            oldSlot.setDate(newSlot.getDate());
            oldSlot.setTimeFrom(newSlot.getTimeFrom());
            oldSlot.setTimeTo(newSlot.getTimeTo());
            oldSlot.setInstructor(newSlot.getInstructor());
            oldSlot.setCar(newSlot.getCar());
            oldSlot.setStudent(newSlot.getStudent());
            oldSlot.setBooked(true);
            scheduleSlotRepository.save(oldSlot);
        }

        return true;
    }

}
