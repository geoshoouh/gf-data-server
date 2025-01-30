package com.gf.server.entities;

import com.gf.server.enumerations.ExerciseEnum;
import com.gf.server.enumerations.EquipmentEnum;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name="EXERCISE_INSTANCES")
@Data
public class ExerciseRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    private Long trainerId;
    
    @ManyToOne
    private GF_Client client;

    EquipmentEnum equipmentType;
    ExerciseEnum exercise;
    
    int resistance;
    int seatSetting;
    int padSetting;
    int rightArm;
    int leftArm;
}
