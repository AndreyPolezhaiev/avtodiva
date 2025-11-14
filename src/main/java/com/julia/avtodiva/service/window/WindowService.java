package com.julia.avtodiva.service.window;

import com.julia.avtodiva.model.Window;

public interface WindowService {
    void bookWindow(Window window);
    void addFreeWindowsForEachInstructor(int days);
    void addFreeWindowsForInstructor(String instructorName, int days);
    void addFreeWindowsForCar(String carName, int days);
}
