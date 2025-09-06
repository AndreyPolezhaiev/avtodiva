package com.julia.avtodiva.service.instructor;

import com.julia.avtodiva.model.Instructor;
import com.julia.avtodiva.model.Weekend;

import java.util.List;

public interface InstructorService {
    List<Instructor> findAllInstructors();
    void saveInstructor(Instructor instructor);
    void saveAllInstructors(List<Instructor> list);
    Instructor findById(Long id);
    Instructor findByName(String name);
    String[] getInstructorsNames();
    void deleteByName(String name);
}
