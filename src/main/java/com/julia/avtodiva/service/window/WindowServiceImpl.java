package com.julia.avtodiva.service.window;

import com.julia.avtodiva.model.*;
import com.julia.avtodiva.repository.CarRepository;
import com.julia.avtodiva.repository.InstructorRepository;
import com.julia.avtodiva.repository.ScheduleSlotRepository;
import com.julia.avtodiva.repository.WindowRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
public class WindowServiceImpl implements WindowService {
    private final WindowRepository windowRepository;
    private final ScheduleSlotRepository scheduleSlotRepository;
    private final CarRepository carRepository;
    private final InstructorRepository instructorRepository;

    public WindowServiceImpl(WindowRepository windowRepository, ScheduleSlotRepository scheduleSlotRepository, CarRepository carRepository, InstructorRepository instructorRepository) {
        this.windowRepository = windowRepository;
        this.scheduleSlotRepository = scheduleSlotRepository;
        this.carRepository = carRepository;
        this.instructorRepository = instructorRepository;
    }

    @Override
    public void bookWindow(Window window) {
        window.setTimeTo(window.getTimeFrom().plusHours(3));

        Instructor instructor = instructorRepository.findByName(window.getInstructorName())
                .orElseThrow(() -> new RuntimeException("Instructor not found"));
        Car car = carRepository.findByName(window.getCarName())
                .orElseThrow(() -> new RuntimeException("Car not found"));

        // Создаём слот
        ScheduleSlot slot = new ScheduleSlot();
        slot.setDate(window.getDate());
        slot.setTimeFrom(window.getTimeFrom());
        slot.setTimeTo(window.getTimeTo());
        slot.setInstructor(instructor);
        slot.setCar(car);
        slot.setBooked(true);

        scheduleSlotRepository.save(slot);

        windowRepository.save(window);
    }

    private int[][] getWorkingHours(String instructorName, LocalDate date) {
        int[][] fullDay = {
                {8, 0}, {11, 30}, {15, 0}, {18, 30}
        };
        int[][] afternoon = {
                {15, 0}, {18, 30}
        };

        if ("Юлія".equalsIgnoreCase(instructorName)) {
            return afternoon;
        }

        if ("Діна".equalsIgnoreCase(instructorName)) {
            if (date.getDayOfWeek() == java.time.DayOfWeek.MONDAY) {
                return fullDay;
            } else {
                return afternoon;
            }
        }

        return fullDay;
    }

    // Добавление всех новых окон для каждого инструктора со всеми машинами
    @Override
    public void addFreeWindowsForEachInstructor(int days) {
        List<Instructor> allInstructors = instructorRepository.findAll();
        List<Car> allCars = carRepository.findAll();

         // 8:00 , 11:30, 15:00, 18:30 (2 часа)
        for (int i = 0; i < days; i++) {
            LocalDate targetDate = LocalDate.now().plusDays(i);
            for (Instructor instructor : allInstructors) {
                // выбираем рабочие часы в зависимости от инструктора и дня недели
                int[][] hours = getWorkingHours(instructor.getName(), targetDate);

                for (int j = 0; j < hours.length; j++) {
                    LocalTime from = LocalTime.of(hours[j][0], hours[j][1]);
                    LocalTime to = (j == hours.length - 1) ? from.plusHours(2) : from.plusHours(3);

                    for (Car car : allCars) {
                        boolean exists = scheduleSlotRepository.existsByDateAndTimeFromAndInstructorAndCar(
                                targetDate, from, instructor, car
                        );
                        if (exists) continue;

                        ScheduleSlot slot = new ScheduleSlot();
                        slot.setDate(targetDate);
                        slot.setTimeFrom(from);
                        slot.setTimeTo(to);
                        slot.setInstructor(instructor);
                        slot.setCar(car);
                        slot.setStudent(null);
                        slot.setDescription(null);
                        slot.setLink(null);
                        slot.setBooked(false);

                        scheduleSlotRepository.save(slot);
                    }
                }
            }
        }
    }

    @Override
    public void addFreeWindowsForInstructor(String instructorName, int days) {
        Instructor instructor = instructorRepository.findByName(instructorName)
                .orElseThrow(() -> new RuntimeException("Instructor not found"));

        List<Car> allCars = carRepository.findAll();

        for (int i = 0; i < days; i++) {
            LocalDate targetDate = LocalDate.now().plusDays(i);
            int[][] hours = getWorkingHours(instructor.getName(), targetDate);

            for (int j = 0; j < hours.length; j++) {
                LocalTime from = LocalTime.of(hours[j][0], hours[j][1]);
                LocalTime to = (j == hours.length - 1) ? from.plusHours(2) : from.plusHours(3);

                for (Car car : allCars) {
                    boolean exists = scheduleSlotRepository.existsByDateAndTimeFromAndInstructorAndCar(
                            targetDate, from, instructor, car
                    );
                    if (exists) continue;

                    ScheduleSlot slot = new ScheduleSlot();
                    slot.setDate(targetDate);
                    slot.setTimeFrom(from);
                    slot.setTimeTo(to);
                    slot.setInstructor(instructor);
                    slot.setCar(car);
                    slot.setBooked(false);

                    scheduleSlotRepository.save(slot);
                }
            }
        }
    }

    @Override
    public void addFreeWindowsForCar(String carName, int days) {
        Car car = carRepository.findByName(carName)
                .orElseThrow(() -> new RuntimeException("Car not found"));

        List<Instructor> allInstructors = instructorRepository.findAll();

        for (int i = 0; i < days; i++) {
            LocalDate targetDate = LocalDate.now().plusDays(i);
            for (Instructor instructor : allInstructors) {
                // учитываем график инструктора
                int[][] hours = getWorkingHours(instructor.getName(), targetDate);

                for (int j = 0; j < hours.length; j++) {
                    LocalTime from = LocalTime.of(hours[j][0], hours[j][1]);
                    LocalTime to = (j == hours.length - 1) ? from.plusHours(2) : from.plusHours(3);

                    boolean exists = scheduleSlotRepository.existsByDateAndTimeFromAndInstructorAndCar(
                            targetDate, from, instructor, car
                    );
                    if (exists) continue;

                    ScheduleSlot slot = new ScheduleSlot();
                    slot.setDate(targetDate);
                    slot.setTimeFrom(from);
                    slot.setTimeTo(to);
                    slot.setInstructor(instructor);
                    slot.setCar(car);
                    slot.setBooked(false);

                    scheduleSlotRepository.save(slot);
                }
            }
        }
    }
}
