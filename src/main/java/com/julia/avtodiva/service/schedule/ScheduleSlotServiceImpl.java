package com.julia.avtodiva.service.schedule;

import com.julia.avtodiva.model.Car;
import com.julia.avtodiva.model.Instructor;
import com.julia.avtodiva.model.ScheduleSlot;
import com.julia.avtodiva.model.Student;
import com.julia.avtodiva.repository.CarRepository;
import com.julia.avtodiva.repository.InstructorRepository;
import com.julia.avtodiva.repository.ScheduleSlotRepository;
import com.julia.avtodiva.repository.StudentRepository;
import com.julia.avtodiva.service.window.WorkingHoursProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class ScheduleSlotServiceImpl implements ScheduleSlotService {
    private final ScheduleSlotRepository scheduleSlotRepository;
    private final InstructorRepository instructorRepository;
    private final CarRepository carRepository;

    @Override
    public List<ScheduleSlot> findAllSlots(List<String> instructorNames, List<String> carNames, LocalDate start, LocalDate end) {
        List<String> instructors = instructorNames == null ? List.of() :
                instructorNames.stream().map(String::toLowerCase).toList();
        List<String> cars = carNames == null ? List.of() :
                carNames.stream().map(String::toLowerCase).toList();

        List<ScheduleSlot> allSlotsFromRepo = scheduleSlotRepository.findAllSlots(instructors, cars, start, end);

        return allSlotsFromRepo;
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

        return freeSlotsFromRepo;
    }

    @Override
    @CacheEvict(value = "studentNames", allEntries = true)
    public void updateSlot(ScheduleSlot slot) {
        scheduleSlotRepository.save(slot);
    }

    private void createExceptionSlot(ScheduleSlot exceptionSlot) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("EEEE, dd.MM.yyyy", new Locale("uk", "UA"));

        if (exceptionSlot.getTimeFrom().isAfter(LocalTime.of(14,59))
                && !exceptionSlot.getInstructor().getName().equalsIgnoreCase("Марина")) {
            throw new IllegalStateException("Помилка: У інструктора "
                    + exceptionSlot.getInstructor().getName()
                    + " не має місць на "
                    + exceptionSlot.getDate().format(dateFormatter)
                    + " о "
                    + exceptionSlot.getTimeFrom().toString());
        }

        Instructor instructor = instructorRepository.findByName(exceptionSlot.getInstructor().getName()).orElse(null);
        Car car = carRepository.findByName(exceptionSlot.getCar().getName()).orElse(null);

        if (instructor != null && scheduleSlotRepository.existsWeekendConflict(
                instructor,
                exceptionSlot.getDate(),
                exceptionSlot.getTimeFrom(),
                exceptionSlot.getTimeTo()
        )) {
            throw new IllegalStateException("Виключний слот інструктора потрапляє у вихідний/неробочий час!");
        }

        if (car != null && scheduleSlotRepository.existsBookedInstructorAndCarConflict(
                instructor,
                car,
                exceptionSlot.getDate(),
                exceptionSlot.getTimeFrom(),
                exceptionSlot.getTimeTo()
        )) {
            throw new IllegalStateException("Ця машина або інструктор вже зайнята у цей час!");
        }
//
//        if (scheduleSlotRepository.existsBookedInstructorConflict(
//                instructor,
//                exceptionSlot.getDate(),
//                exceptionSlot.getTimeFrom(),
//                exceptionSlot.getTimeTo()
//        )) {
//            throw new IllegalStateException("Цей інструктор вже зайнят у цей час!");
//        }

        exceptionSlot.setInstructor(instructor);
        exceptionSlot.setCar(car);
        scheduleSlotRepository.save(exceptionSlot);
    }

    @Override
    @Transactional
    @CacheEvict(value = "studentNames", allEntries = true)
    public void createSlot(ScheduleSlot newSlot) {
        ScheduleSlot existing = scheduleSlotRepository.findByInstructorCarDateTime(
                newSlot.getInstructor().getName().toLowerCase(),
                newSlot.getCar().getName().toLowerCase(),
                newSlot.getDate(),
                newSlot.getTimeFrom()
        ).orElse(null);

        if (existing != null) {
            if (existing.getStudent() != null) {
                DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("EEEE, dd.MM.yyyy", new Locale("uk", "UA"));

                throw new IllegalStateException("Помилка: Запис на "
                        + existing.getDate().format(dateFormatter)
                        + " о "
                        + existing.getTimeFrom().toString()
                        + " вже існує!");
            }
            existing.setStudent(newSlot.getStudent());
            existing.setDescription(newSlot.getDescription());
            existing.setLink(newSlot.getLink());
            existing.setBooked(newSlot.isBooked());
            rescheduleSlot(existing);

        } else if (newSlot.getInstructor().getName().equalsIgnoreCase("Юлія")) {
            createExceptionSlot(newSlot);

        } else if (newSlot.getInstructor().getName().equalsIgnoreCase("Марина")) {
            createExceptionSlot(newSlot);

        } else {
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("EEEE, dd.MM.yyyy", new Locale("uk", "UA"));

            throw new IllegalStateException("Помилка: У інструктора "
                    + newSlot.getInstructor().getName()
                    + " не має місць на "
                    + newSlot.getDate().format(dateFormatter)
                    + " о "
                    + newSlot.getTimeFrom().toString());
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = "studentNames", allEntries = true)
    public boolean rescheduleSlot(ScheduleSlot slot) {
        // достаём актуальный слот из базы по id
        ScheduleSlot existing = scheduleSlotRepository.findById(slot.getId())
                .orElseThrow(() -> new IllegalArgumentException("Slot not found"));
        Car carFromRepo = carRepository.findByName(slot.getCar().getName())
                .orElseThrow(() -> new IllegalArgumentException("Slot not found"));

        // проверяем, изменились ли ключевые поля
        boolean changed =
                !existing.getInstructor().getName().equalsIgnoreCase(slot.getInstructor().getName()) ||
                        !existing.getCar().getName().equalsIgnoreCase(slot.getCar().getName()) ||
                        !existing.getDate().equals(slot.getDate()) ||
                        !existing.getTimeFrom().equals(slot.getTimeFrom());

        Instructor instructor = instructorRepository.findByName(slot.getInstructor().getName()).orElse(null);
        if (instructor != null && scheduleSlotRepository.existsWeekendConflict(
                instructor,
                slot.getDate(),
                slot.getTimeFrom(),
                slot.getTimeTo()
        )) {
            throw new IllegalStateException("Новий слот інструктора потрапляє у вихідний/неробочий час!");
        }

        if (!changed) {
            // проверка: у машины не должно быть других занятых слотов в это время
//            if (scheduleSlotRepository.existsBookedCarConflictExcluding(
//                    carFromRepo,
//                    slot.getDate(),
//                    slot.getTimeFrom(),
//                    slot.getTimeTo(),
//                    existing.getId() // исключаем сам slot, иначе он всегда даст конфликт
//            )) {
//                throw new IllegalStateException("Ця машина вже зайнята у цей час!");
//            }

            if (scheduleSlotRepository.existsBookedInstructorAndCarConflictExcluding(
                    instructor,
                    carFromRepo,
                    slot.getDate(),
                    slot.getTimeFrom(),
                    slot.getTimeTo(),
                    existing.getId()
            )) {
                throw new IllegalStateException("Цей інструктор або машина вже зайняті у цей час!");
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
                    carFromRepo,
                    slot.getDate(),
                    slot.getTimeFrom(),
                    slot.getTimeTo(),
                    target.getId() // исключаем сам slot, иначе он всегда даст конфликт
            )) {
                throw new IllegalStateException("Ця машина вже зайнята у цей час!");
            }

            if (scheduleSlotRepository.existsBookedInstructorConflictExcluding(
                    instructor,
                    slot.getDate(),
                    slot.getTimeFrom(),
                    slot.getTimeTo(),
                    existing.getId()
            )) {
                throw new IllegalStateException("Цей інструктор вже зайнят у цей час!");
            }

            // занимаем найденный слот
            target.setBooked(true);
            target.setStudent(slot.getStudent());
            target.setDescription(slot.getDescription());
            target.setLink(slot.getLink());
            scheduleSlotRepository.save(target);
            return true;
        } else {

//            if (scheduleSlotRepository.existsBookedCarConflictExcluding(
//                    carFromRepo,
//                    slot.getDate(),
//                    slot.getTimeFrom(),
//                    slot.getTimeTo(),
//                    null
//            )) {
//                throw new IllegalStateException("Ця машина вже зайнята у цей час!");
//            }

            if (scheduleSlotRepository.existsBookedInstructorAndCarConflict(
                    instructor,
                    carFromRepo,
                    slot.getDate(),
                    slot.getTimeFrom(),
                    slot.getTimeTo()
            )) {
                throw new IllegalStateException("Цей інструктор або машина вже зайняті у цей час!");
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
        return scheduleSlotRepository.findByStudentNameIgnoreCase(studentName.trim());
    }

    @Override
    public List<ScheduleSlot> filterSlotsByTime(List<ScheduleSlot> slots, List<String> selectedTimes) {
        return slots.stream()
                .filter(slot -> selectedTimes.contains(slot.getTimeFrom().toString()))
                .toList();
    }

    @Override
    public List<ScheduleSlot> findAllBookedSlotsByInstructorName(String instructorName) {
        return scheduleSlotRepository.findAllBookedSlotsByInstructorName(instructorName);
    }
}
