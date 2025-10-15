package com.julia.avtodiva.service.instructor;

import com.julia.avtodiva.model.Instructor;
import com.julia.avtodiva.repository.InstructorRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        if (instructor == null || instructor.getName() == null || instructor.getName().isBlank()) {
            throw new IllegalArgumentException("Ім'я інструктора не може бути порожнім!");
        }

        if (instructorRepository.existsByNameIgnoreCase(instructor.getName())) {
            throw new IllegalStateException("Інструктор з іменем '" + instructor.getName() + "' вже існує!");
        }
        instructor.setName(instructor.getName());
        instructorRepository.save(instructor);
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

    @Override
    public String[] getInstructorsNames() {
        return instructorRepository.findAll()
                .stream()
                .map(Instructor::getName)
                .toArray(String[]::new);
    }

    @Override
    @Transactional
    public void deleteByName(String name) {
        instructorRepository.deleteByName(name);
    }
}
