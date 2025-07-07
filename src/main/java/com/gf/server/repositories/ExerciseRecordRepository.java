package com.gf.server.repositories;

import java.util.List;
import java.util.Optional;
import java.util.Date;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gf.server.entities.ExerciseRecord;
import com.gf.server.entities.GF_Client;
import com.gf.server.enumerations.EquipmentEnum;
import com.gf.server.enumerations.ExerciseEnum;

public interface ExerciseRecordRepository extends JpaRepository<ExerciseRecord, Long>{

    Optional<List<ExerciseRecord>> findByClientAndEquipmentTypeAndExerciseOrderByDateTime(GF_Client client, EquipmentEnum equipment, ExerciseEnum exercise);
    
    Optional<List<ExerciseRecord>> findByClientAndEquipmentTypeAndExerciseAndDateTimeAfterOrderByDateTimeDesc(GF_Client client, EquipmentEnum equipment, ExerciseEnum exercise, Date afterDate);
}
