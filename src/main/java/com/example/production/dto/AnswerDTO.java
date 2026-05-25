package com.example.production.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AnswerDTO {

    @NotNull(message = "questionId không được để trống")
    private Long questionId;

    @NotNull(message = "score không được để trống")
    @Min(value = 1, message = "score phải từ 1 đến 5")
    @Max(value = 5, message = "score phải từ 1 đến 5")
    private Integer score;


}
