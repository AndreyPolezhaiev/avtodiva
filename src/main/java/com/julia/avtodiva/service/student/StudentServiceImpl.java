package com.julia.avtodiva.service.student;

import com.julia.avtodiva.model.Student;
import com.julia.avtodiva.repository.ScheduleSlotRepository;
import com.julia.avtodiva.repository.StudentRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class StudentServiceImpl implements StudentService {
    private final StudentRepository studentRepository;
    private final ScheduleSlotRepository scheduleSlotRepository;

    // students that are being taught and belong to at least one slot
    @Override
    public String[] getStudentsNames() {
        return studentRepository.findAll()
                .stream()
                .filter(scheduleSlotRepository::existsByStudent)
                .map(Student::getName)
                .distinct()
                .toArray(String[]::new);
    }
}
