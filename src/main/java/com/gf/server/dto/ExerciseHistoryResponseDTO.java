package com.gf.server.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.gf.server.entities.ExerciseRecord;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public record ExerciseHistoryResponseDTO(
    String message,
    List<ExerciseRecord> exerciseRecords
) {} 