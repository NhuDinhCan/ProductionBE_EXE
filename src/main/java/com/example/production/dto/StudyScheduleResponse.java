package com.example.production.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Builder
public class StudyScheduleResponse {
    private Long id;
    private Long careerId;
    private String careerName;
    private Long learningMethodId;
    private String learningMethodTitle;
    private String title;
    private String description;
    private String dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;
    private String status;
    private LocalDateTime createdAt;
}
