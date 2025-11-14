package com.julia.avtodiva.ui.state;

import lombok.Data;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@Data
public class AppState {
    public static LocalDate startDate;
    public static LocalDate endDate;
    public static List<String> instructorNames;
    public static List<String> carNames;
    public static String[] COLUMNS = {"✓", "Дата", "Інструктор", "Машина", "Час з", "Час до", "Учениця", "Опис", "Посилання"};
    public static int[][] DEFAULT_HOURS = {{8,0}, {11,30}, {15,0}, {18,15}};
    public static String[] DEFAULT_HOURS_STR = {"08:00", "11:30", "15:00", "16:00", "18:15"};
    public static int COLUMN_HEIGHT = 28;
}
