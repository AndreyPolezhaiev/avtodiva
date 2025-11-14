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
    public static int[][] DEFAULT_HOURS = {{7,0}, {10,30}, {14,0}, {17,15}};
    public static String[] DEFAULT_HOURS_STR = {"07:00", "10:30", "14:00", "15:00", "17:15"};
    public static int COLUMN_HEIGHT = 28;
}
