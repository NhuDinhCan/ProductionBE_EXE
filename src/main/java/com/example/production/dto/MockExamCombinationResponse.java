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
public class MockExamCombinationResponse {
    private Long id;
    private String code;
    private String name;
    private List<MockExamSubjectResponse> subjects;
}
