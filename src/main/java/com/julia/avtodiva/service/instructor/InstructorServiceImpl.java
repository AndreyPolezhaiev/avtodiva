package com.julia.avtodiva.service.instructor;

import com.julia.avtodiva.model.Instructor;
import com.julia.avtodiva.repository.InstructorRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class InstructorServiceImpl implements InstructorService {
    private final InstructorRepository instructorRepository;

    @Override
    public List<Instructor> findAllInstructors() {
        return instructorRepository.findAll();
    }

    @Override
    public void saveInstructor(Instructor instructor) {
        if (instructor != null) {
            instructorRepository.save(instructor);
        }
    }

    @Override
    public void saveAllInstructors(List<Instructor> instructors) {
        instructorRepository.saveAll(instructors);
    }

    @Override
    public Instructor findById(Long id) {
        return instructorRepository.findById(id).orElseThrow(
                () -> new RuntimeException("Can't find instructor by id: " + id)
        );
    }

    @Override
    public Instructor findByName(String name) {
        return instructorRepository.findByName(name).orElseThrow(
                () -> new RuntimeException("Can't find instructor by name: " + name)
        );
    }
}
