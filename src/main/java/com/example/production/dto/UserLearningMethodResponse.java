package com.example.production.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserLearningMethodResponse {
    private Long id;
    private Long careerId;
    private String careerName;
    private Long learningMethodId;
    private String learningMethodTitle;
    private String learningMethodDescription;
    private String status;
    private LocalDateTime appliedAt;
    private boolean alreadyApplied;
}
