package com.julia.avtodiva.service.schedule;

import com.julia.avtodiva.model.ScheduleSlot;
import com.julia.avtodiva.model.Student;
import com.julia.avtodiva.repository.ScheduleSlotRepository;
import com.julia.avtodiva.repository.StudentRepository;
import com.julia.avtodiva.service.window.WorkingHoursProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ScheduleSlotServiceImpl implements ScheduleSlotService {
    private final ScheduleSlotRepository scheduleSlotRepository;

    @Override
    public List<ScheduleSlot> findAllSlots(List<String> instructorNames, List<String> carNames, LocalDate start, LocalDate end) {
        List<String> instructors = instructorNames == null ? List.of() :
                instructorNames.stream().map(String::toLowerCase).toList();
        List<String> cars = carNames == null ? List.of() :
                carNames.stream().map(String::toLowerCase).toList();

        List<ScheduleSlot> allSlotsFromRepo = scheduleSlotRepository.findAllSlots(instructors, cars, start, end);

        return allSlotsFromRepo.stream()
                .filter(slot -> {
                    int[][] workingHours = WorkingHoursProvider.getWorkingHours(
                            slot.getInstructor().getName(),
                            slot.getDate()
                    );
                    return Arrays.stream(workingHours)
                            .anyMatch(time -> slot.getTimeFrom().getHour() == time[0]
                                    && slot.getTimeFrom().getMinute() == time[1]);
                })
                .toList();
    }

    @Override
    public List<ScheduleSlot> findBookedSlots(List<String> instructorNames, List<String> carNames, LocalDate start, LocalDate end) {
        List<String> instructors = instructorNames == null ? List.of() :
                instructorNames.stream().map(String::toLowerCase).toList();
        List<String> cars = carNames == null ? List.of() :
                carNames.stream().map(String::toLowerCase).toList();

        return scheduleSlotRepository.findBookedSlotsBetween(instructors, cars, start, end);
    }

    @Override
    public List<ScheduleSlot> findFreeSlots(List<String> instructorNames, List<String> carNames, LocalDate start, LocalDate end) {
        List<String> instructors = instructorNames == null ? List.of() :
                instructorNames.stream().map(String::toLowerCase).toList();
        List<String> cars = carNames == null ? List.of() :
                carNames.stream().map(String::toLowerCase).toList();

        List<ScheduleSlot> freeSlotsFromRepo = scheduleSlotRepository.findFreeSlotsBetween(instructors, cars, start, end);

        return freeSlotsFromRepo.stream()
                .filter(slot -> {
                    int[][] workingHours = WorkingHoursProvider.getWorkingHours(
                            slot.getInstructor().getName(),
                            slot.getDate()
                    );
                    return Arrays.stream(workingHours)
                            .anyMatch(time -> slot.getTimeFrom().getHour() == time[0]
                                    && slot.getTimeFrom().getMinute() == time[1]);
                })
                .toList();
    }

    @Override
    public void updateSlot(ScheduleSlot slot) {
        scheduleSlotRepository.save(slot);
    }

    @Override
    @Transactional
    public boolean rescheduleSlot(ScheduleSlot slot) {
        // достаём актуальный слот из базы по id
        ScheduleSlot existing = scheduleSlotRepository.findById(slot.getId())
                .orElseThrow(() -> new IllegalArgumentException("Slot not found"));

        // проверяем, изменились ли ключевые поля
        boolean changed =
                !existing.getInstructor().getName().equalsIgnoreCase(slot.getInstructor().getName()) ||
                        !existing.getCar().getName().equalsIgnoreCase(slot.getCar().getName()) ||
                        !existing.getDate().equals(slot.getDate()) ||
                        !existing.getTimeFrom().equals(slot.getTimeFrom());

        if (!changed) {
            // проверка: у машины не должно быть других занятых слотов в это время
            if (scheduleSlotRepository.existsBookedCarConflictExcluding(
                    slot.getCar(),
                    slot.getDate(),
                    slot.getTimeFrom(),
                    slot.getTimeTo(),
                    existing.getId() // исключаем сам slot, иначе он всегда даст конфликт
            )) {
                throw new IllegalStateException("Ця машина вже зайнята у цей час!");
            }
            // ничего важного не изменилось → просто обновляем
            existing.setDescription(slot.getDescription());
            existing.setLink(slot.getLink());
            existing.setStudent(slot.getStudent());
            existing.setBooked(slot.isBooked());
            scheduleSlotRepository.save(existing);
            return true;
        }

        // 1. освобождаем старый слот
        existing.setBooked(false);
        existing.setStudent(null);
        existing.setDescription("Якщо запис є у вільних місцях то можна зайняти, інакше буде помилка");
        existing.setLink("Якщо запис є у вільних місцях то можна зайняти, інакше буде помилка");
        scheduleSlotRepository.save(existing);

        // 2. ищем слот с новыми параметрами
        ScheduleSlot target = scheduleSlotRepository.findByInstructorCarDateTime(
                slot.getInstructor().getName().toLowerCase(),
                slot.getCar().getName().toLowerCase(),
                slot.getDate(),
                slot.getTimeFrom()
        ).orElse(null);

        if (target != null) {
            if (target.isBooked()) {
                throw new IllegalStateException("Target slot already booked!");
            }

            // проверка: у машины не должно быть других занятых слотов в это время
            if (scheduleSlotRepository.existsBookedCarConflictExcluding(
                    slot.getCar(),
                    slot.getDate(),
                    slot.getTimeFrom(),
                    slot.getTimeTo(),
                    target.getId() // исключаем сам slot, иначе он всегда даст конфликт
            )) {
                throw new IllegalStateException("Ця машина вже зайнята у цей час!");
            }

            // занимаем найденный слот
            target.setBooked(true);
            target.setStudent(slot.getStudent());
            target.setDescription(slot.getDescription());
            target.setLink(slot.getLink());
            scheduleSlotRepository.save(target);
            return true;
        } else {
            // проверка: у машины не должно быть других занятых слотов в это время
            if (scheduleSlotRepository.existsBookedCarConflictExcluding(
                    slot.getCar(),
                    slot.getDate(),
                    slot.getTimeFrom(),
                    slot.getTimeTo(),
                    target.getId() // исключаем сам slot, иначе он всегда даст конфликт
            )) {
                throw new IllegalStateException("Ця машина вже зайнята у цей час!");
            }

            // если такого слота ещё нет → создаём новый
            ScheduleSlot created = new ScheduleSlot();
            created.setDate(slot.getDate());
            created.setTimeFrom(slot.getTimeFrom());
            created.setTimeTo(slot.getTimeTo());
            created.setInstructor(slot.getInstructor());
            created.setCar(slot.getCar());
            created.setStudent(slot.getStudent());
            created.setDescription(slot.getDescription());
            created.setLink(slot.getLink());
            created.setBooked(true);
            scheduleSlotRepository.save(created);
            return true;
        }
    }

    @Override
    public List<ScheduleSlot> findByInstructorAndStudentNames(String instructorName, String studentName) {
        return scheduleSlotRepository.findByInstructorNameIgnoreCaseAndStudentNameIgnoreCase(instructorName, studentName);
    }

    @Override
    public List<ScheduleSlot> findByInstructorName(String instructorName) {
        return scheduleSlotRepository.findByInstructorNameIgnoreCase(instructorName);
    }

    @Override
    public List<ScheduleSlot> findByStudentName(String studentName) {
        return scheduleSlotRepository.findByStudentNameIgnoreCase(studentName);
    }
}
