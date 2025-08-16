package com.julia.avtodiva.service.instructor;

import com.julia.avtodiva.model.Instructor;

import java.util.List;

public interface InstructorService {
    List<Instructor> findAllInstructors();
    void updateInstructor(Instructor instructor);
}
