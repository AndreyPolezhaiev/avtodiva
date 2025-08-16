package com.julia.avtodiva.ui.state;

import lombok.Data;
import org.springframework.stereotype.Component;

@Component
@Data
public class AppState {
    public static int daysAhead = 7;
    public static String instructorName;
    public static String carName;
    public static String[] COLUMNS = {"✓", "Дата", "Инструктор", "Машина", "Время с", "Время до", "Ученица", "Описание", "Ссылка"};
    public static int[][] DEFAULT_HOURS = {{8,0}, {11,30}, {15,0}, {18,30}};
}
