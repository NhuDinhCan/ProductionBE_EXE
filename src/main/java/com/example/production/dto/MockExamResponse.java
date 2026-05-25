package com.example.production.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MockExamResponse {
    private Long id;
    private String title;
    private Long subjectId;
    private String subjectCode;
    private String subjectName;
    private Long combinationId;
    private String combinationCode;
    private String combinationName;
    private String difficulty;
    private Integer year;
    private Integer durationMinutes;
    private Integer totalQuestions;
    private List<MockExamQuestionResponse> questions;
}
