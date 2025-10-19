package com.julia.avtodiva.repository;

import com.julia.avtodiva.model.Car;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CarRepository extends JpaRepository<Car, Long> {
    Optional<Car> findByName(String name);
    void deleteByName(String name);
    boolean existsByNameIgnoreCase(String name);

    @Query("select c.name from Car c where c.name is not null")
    List<String> findAllCarNames();
}