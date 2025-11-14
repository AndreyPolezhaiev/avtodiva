package com.julia.avtodiva.service.car;

import com.julia.avtodiva.model.Car;

public interface CarService {
    void saveCar(Car car);
    void deleteByName(String name);
    String[] getCarsNames();
    Car findByName(String name);
}
