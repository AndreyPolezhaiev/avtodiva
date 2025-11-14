package com.julia.avtodiva.service.weekend;

import com.julia.avtodiva.model.Weekend;
import com.julia.avtodiva.repository.WeekendRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@AllArgsConstructor
public class WeekendServiceImpl implements WeekendService {
    private final WeekendRepository weekendRepository;

    @Override
    public void saveAllWeekends(List<Weekend> weekends) {
        weekendRepository.saveAll(weekends);
    }

    @Override
    public void deleteAllWeekends(List<Weekend> weekends) {
        weekendRepository.deleteAll(weekends);
    }

    @Override
    public void save(Weekend weekend) {
        weekendRepository.save(weekend);
    }
}
