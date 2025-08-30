package com.julia.avtodiva.ui.state;

import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Data
public class AppState {
    public static int daysAhead = 7;
    public static List<String> instructorNames;
    public static List<String> carNames;
    public static String[] COLUMNS = {"✓", "Дата", "Інструктор", "Машина", "Час з", "Час до", "Учениця", "Опис", "Посилання"};
    public static int[][] DEFAULT_HOURS = {{8,0}, {11,30}, {15,0}, {18,30}};
    public static int COLUMN_HEIGHT = 28;
    public static String[] INSTRUCTORS = {"Таня", "Марина", "Христина", "Ксюша", "Іра", "Юля"};
    public static String[] CARS = {"Фея", "Мерлин", "Мавка", "Фрея", "На своїй"};
}
