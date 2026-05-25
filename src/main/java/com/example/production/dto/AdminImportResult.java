package com.example.production.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AdminImportResult {
    private boolean success;
    private int totalRows;
    private int importedCount;
    private int updatedCount;
    private List<String> errors;
    private List<Map<String, Object>> items;
}
