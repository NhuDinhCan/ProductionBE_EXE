package com.example.production.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UniversityRequestDTO {

    @NotNull(message = "careerId không được để trống")
    private Long careerId;

    @NotNull(message = "score không được để trống")
    @PositiveOrZero(message = "score không được âm")
    private Double score;

}
