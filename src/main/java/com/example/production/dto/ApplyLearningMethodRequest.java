package com.example.production.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ApplyLearningMethodRequest {

    @NotNull(message = "careerId không được để trống")
    private Long careerId;

    @NotNull(message = "learningMethodId không được để trống")
    private Long learningMethodId;
}
