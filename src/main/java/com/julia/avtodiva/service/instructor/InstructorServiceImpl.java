package com.julia.avtodiva.service.instructor;

import com.julia.avtodiva.model.Instructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InstructorServiceImpl implements InstructorService {
    @Override
    public List<Instructor> findAllInstructors() {
        return List.of();
    }

    @Override
    public void updateInstructor(Instructor instructor) {

    }
}
