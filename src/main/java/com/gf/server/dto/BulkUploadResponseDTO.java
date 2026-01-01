package com.gf.server.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public record BulkUploadResponseDTO(
    String message,
    int totalRecords,
    int successfulRecords,
    int failedRecords,
    List<String> errors
) {}
