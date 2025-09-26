package com.julia.avtodiva.ui.util;

public class CarNameParser {
    public static String parseCarName(String carName) {
        if (carName.equalsIgnoreCase("Мавка")) {
            return "Honda";
        } else if (carName.equalsIgnoreCase("Мерлин")) {
            return "Subaru";
        } else if (carName.equalsIgnoreCase("Фрея")) {
            return "Toyota";
        } else if (carName.equalsIgnoreCase("Фея")) {
            return "Ford";
        }
        return "Tesla";
    }
}
