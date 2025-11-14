package com.julia.avtodiva.repository;

import com.julia.avtodiva.model.Weekend;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WeekendRepository extends JpaRepository<Weekend, Long> {
}
