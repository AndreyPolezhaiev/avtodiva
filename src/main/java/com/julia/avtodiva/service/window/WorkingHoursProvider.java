package com.julia.avtodiva.service.window;

import java.time.DayOfWeek;
import java.time.LocalDate;

public class WorkingHoursProvider {

    public static int[][] getWorkingHours(String instructorName, LocalDate date) {
        int[][] fullDay = {
                {8, 0}, {11, 30}, {15, 0}
        };
        int[][] afternoon = {
                {15, 0}, {18, 15}
        };
        int[][] upWork = {
                {8, 0}, {11, 30}, {15, 0}, {18, 15}
        };

        if ("Юлія".equalsIgnoreCase(instructorName)) {
            return afternoon;
        }

        if ("Діна".equalsIgnoreCase(instructorName)) {
            if (date.getDayOfWeek() == DayOfWeek.MONDAY) {
                return upWork;
            }
            else {
                return afternoon;
            }
        }

        return fullDay;
    }
}
