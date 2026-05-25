package com.example.production.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LearningStrategyResponse {
    private Long careerId;
    private String major;
    private String description;
    private List<LearningMethodDTO> methods;
    private List<String> skills;
    private List<String> tools;
    private List<String> weeklyRoadmap;
}
