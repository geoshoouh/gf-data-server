package com.gf.server.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.gf.server.entities.ExerciseRecord;
import com.gf.server.entities.GF_Client;
import com.gf.server.enumerations.EquipmentEnum;
import com.gf.server.enumerations.ExerciseEnum;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public record ReqResDTO(
    String message,
    Long trainerId,
    EquipmentEnum equipmentType,
    ExerciseEnum exerciseType,
    GF_Client client,
    ExerciseRecord exerciseInstance
) {} 
