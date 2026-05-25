package com.example.production.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalTime;

@Data
public class StudyScheduleRequest {

    @NotNull(message = "careerId không được để trống")
    private Long careerId;

    private Long learningMethodId;

    @NotBlank(message = "title không được để trống")
    @Size(max = 255, message = "title không được vượt quá 255 ký tự")
    private String title;

    @Size(max = 1000, message = "description không được vượt quá 1000 ký tự")
    private String description;

    @NotBlank(message = "dayOfWeek không được để trống")
    private String dayOfWeek;

    @NotNull(message = "startTime không được để trống")
    private LocalTime startTime;

    @NotNull(message = "endTime không được để trống")
    private LocalTime endTime;
}
