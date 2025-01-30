package com.gf.server.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gf.server.entities.ExerciseRecord;

public interface ExerciseRecordRepository extends JpaRepository<ExerciseRecord, Long>{
    
}
