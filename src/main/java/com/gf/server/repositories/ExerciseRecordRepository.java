package com.gf.server.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gf.server.entities.ExerciseInstance;

public interface ExerciseRecordRepository extends JpaRepository<ExerciseInstance, Long>{
    
}
