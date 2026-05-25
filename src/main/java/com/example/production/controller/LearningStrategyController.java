package com.example.production.controller;

import com.example.production.dto.LearningStrategyResponse;
import com.example.production.dto.MajorDTO;
import com.example.production.service.LearningStrategyService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/learning-strategies")
@RequiredArgsConstructor
public class LearningStrategyController {

    private final LearningStrategyService learningStrategyService;

    @GetMapping("/majors")
    public List<MajorDTO> getMajors() {
        return learningStrategyService.getMajors();
    }

    @GetMapping
    public LearningStrategyResponse getStrategy(@RequestParam String major) {
        return learningStrategyService.getStrategy(major);
    }
}
