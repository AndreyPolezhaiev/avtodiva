package com.julia.avtodiva.service.student;

import com.julia.avtodiva.repository.ScheduleSlotRepository;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class StudentServiceImpl implements StudentService {
    private final ScheduleSlotRepository scheduleSlotRepository;

    // students that are being taught and belong to at least one slot
    @Override
    @Cacheable("studentNames")
    public String[] getStudentsNames() {
        List<String> names = scheduleSlotRepository.findDistinctStudentNames();
        return names.toArray(new String[0]);
    }
}
