package com.example.production.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MockExamStartResponse {
    private Long attemptId;
    private String status;
    private LocalDateTime startedAt;
    private Long remainingSeconds;
    private MockExamResponse exam;
    private List<MockExamSubmitRequest.AnswerRequest> selectedAnswers;
}
