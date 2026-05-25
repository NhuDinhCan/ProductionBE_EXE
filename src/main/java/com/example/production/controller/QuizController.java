package com.example.production.controller;


import com.example.production.dto.AnswerDTO;
import com.example.production.dto.CareerResultDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.production.service.QuizService;

import java.util.List;

@RestController
@RequestMapping("/api/quiz")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;

    @PostMapping("/result")
    public List<CareerResultDTO> calculateResult(@Valid @RequestBody List<@Valid AnswerDTO> answers){
        return quizService.calculateResult(answers);
    }
}
