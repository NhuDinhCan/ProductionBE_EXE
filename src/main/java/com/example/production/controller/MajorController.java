package com.example.production.controller;

import com.example.production.dto.MajorDTO;
import com.example.production.service.LearningStrategyService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/majors")
@RequiredArgsConstructor
public class MajorController {

    private final LearningStrategyService learningStrategyService;

    @GetMapping
    public List<MajorDTO> getMajors() {
        return learningStrategyService.getMajors();
    }
}