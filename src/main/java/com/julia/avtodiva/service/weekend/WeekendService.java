package com.julia.avtodiva.service.weekend;

import com.julia.avtodiva.model.Weekend;

import java.util.List;

public interface WeekendService {
    void saveAllWeekends(List<Weekend> weekends);
    void deleteAllWeekends(List<Weekend> weekends);
    void save(Weekend weekend);
}
