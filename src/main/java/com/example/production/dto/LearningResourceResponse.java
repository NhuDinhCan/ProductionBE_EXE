package com.example.production.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LearningResourceResponse {
    private Long id;
    private Long careerId;
    private String careerName;
    private String title;
    private String description;
    private String resourceType;
    private String level;
    private String url;
    private String thumbnailUrl;
    private String status;
    private boolean saved;
    private LocalDateTime savedAt;
    private LocalDateTime createdAt;
}
