package com.julia.avtodiva.service.car;

import com.julia.avtodiva.model.Car;
import com.julia.avtodiva.repository.CarRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class CarServiceImpl implements CarService {
    private final CarRepository carRepository;

    @Override
    public void saveCar(Car car) {
        if (car == null || car.getName() == null || car.getName().isBlank()) {
            throw new IllegalArgumentException("Ім'я машини не може бути порожнім!");
        }

        if (carRepository.existsByNameIgnoreCase(car.getName())) {
            throw new IllegalStateException("Машина з іменем '" + car.getName() + "' вже існує!");
        }

        car.setName(car.getName().toLowerCase());
        carRepository.save(car);
    }

    @Override
    public String[] getCarsNames() {
        return carRepository.findAll()
                .stream()
                .map(Car::getName)
                .toArray(String[]::new);
    }

    @Override
    public Car findByName(String name) {
        return carRepository.findByName(name).orElseThrow(() -> new RuntimeException("Can't find car by name: " + name));
    }

    @Override
    @Transactional
    public void deleteByName(String name) {
        carRepository.deleteByName(name);
    }
}
