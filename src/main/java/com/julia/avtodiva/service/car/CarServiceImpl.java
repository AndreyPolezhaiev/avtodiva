package com.julia.avtodiva.service.car;

import com.julia.avtodiva.model.Car;
import com.julia.avtodiva.repository.CarRepository;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
public class CarServiceImpl implements CarService {
    private final CarRepository carRepository;

    @Override
    @Caching(evict = {
            @CacheEvict(value = "carNames", allEntries = true),
            @CacheEvict(value = "carByName", key = "#car.name")
    })
    public void saveCar(Car car) {
        if (car == null || car.getName() == null || car.getName().isBlank()) {
            throw new IllegalArgumentException("Ім'я машини не може бути порожнім!");
        }

        if (carRepository.existsByNameIgnoreCase(car.getName())) {
            throw new IllegalStateException("Машина з іменем '" + car.getName() + "' вже існує!");
        }

        car.setName(car.getName().trim());
        carRepository.save(car);
    }

    @Override
    @Cacheable("carNames")
    public String[] getCarsNames() {
        List<String> names = carRepository.findAllCarNames();
        return names.toArray(new String[0]);
    }

    @Override
    @Cacheable("carByName")
    public Car findByName(String name) {
        return carRepository.findByName(name).orElseThrow(() -> new RuntimeException("Can't find car by name: " + name));
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "carNames", allEntries = true),
            @CacheEvict(value = "carByName", key = "#name")
    })
    @Transactional
    public void deleteByName(String name) {
        carRepository.deleteByName(name);
    }
}
