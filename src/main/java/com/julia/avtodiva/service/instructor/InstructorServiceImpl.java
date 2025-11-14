package com.julia.avtodiva.service.instructor;

import com.julia.avtodiva.model.Instructor;
import com.julia.avtodiva.repository.InstructorRepository;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
public class InstructorServiceImpl implements InstructorService {
    private final InstructorRepository instructorRepository;

    @Override
    @Cacheable("instructors")
    public List<Instructor> findAllInstructors() {
        return instructorRepository.findAll();
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "instructors", allEntries = true),
            @CacheEvict(value = "instructorNames", allEntries = true),
            @CacheEvict(value = "instructorByName", key = "#instructor.name")
    })
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
    @Cacheable("instructorByName")
    public Instructor findByName(String name) {
        return instructorRepository.findByName(name).orElseThrow(
                () -> new RuntimeException("Can't find instructor by name: " + name)
        );
    }

    @Override
    @Cacheable("instructorNames")
    public String[] getInstructorsNames() {
        List<String> names = instructorRepository.findAllInstructorNames();
        return names.toArray(new String[0]);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "instructors", allEntries = true),
            @CacheEvict(value = "instructorNames", allEntries = true),
            @CacheEvict(value = "instructorByName", key = "#name")
    })
    @Transactional
    public void deleteByName(String name) {
        instructorRepository.deleteByName(name);
    }
}
