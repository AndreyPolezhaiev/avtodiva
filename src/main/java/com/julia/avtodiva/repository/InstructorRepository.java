package com.julia.avtodiva.repository;

import com.julia.avtodiva.model.Instructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InstructorRepository extends JpaRepository<Instructor, Long> {
    Optional<Instructor> findByName(String name);
    boolean existsByNameIgnoreCase(String name);
    void deleteByName(String name);

    @Query("select i.name from Instructor i where i.name is not null")
    List<String> findAllInstructorNames();
}