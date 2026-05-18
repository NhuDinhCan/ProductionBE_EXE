package com.example.production.service;


import com.example.production.dto.AnswerDTO;
import com.example.production.dto.CareerResultDTO;
import com.example.production.entity.Career;
import com.example.production.entity.QuestionCareerWeight;
import com.example.production.exception.AppException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.example.production.repositpry.CareerRepository;
import com.example.production.repositpry.QuestionCareerWeightRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuizService {

    private final QuestionCareerWeightRepository weightRepository;
    private final CareerRepository careerRepository;

    public List<CareerResultDTO> calculateResult(List<AnswerDTO> answers) {
        if (answers == null || answers.isEmpty())
            throw AppException.badRequest("Danh sách câu trả lời không được rỗng");

        Map<Long, Integer> answerMap = answers.stream()
                .collect(Collectors.toMap(AnswerDTO::getQuestionId, AnswerDTO::getScore));

        List<QuestionCareerWeight> weights = weightRepository
                .findByQuestionIdIn(new ArrayList<>(answerMap.keySet()));

        Map<Long, Double> careerScore = new HashMap<>();
        for (QuestionCareerWeight w : weights) {
            Integer userScore = answerMap.get(w.getQuestion().getId());
            if (userScore == null) continue;
            careerScore.merge(w.getCareer().getId(), userScore * w.getWeight(), Double::sum);
        }

        // Load tất cả career trong 1 query, tránh N+1
        Map<Long, Career> careerMap = careerRepository.findAllById(careerScore.keySet())
                .stream().collect(Collectors.toMap(Career::getId, c -> c));

        return careerScore.entrySet().stream()
                .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                .map(e -> {
                    Career career = careerMap.get(e.getKey());
                    if (career == null)
                        throw AppException.notFound("Career id=" + e.getKey() + " không tồn tại");
                    return CareerResultDTO.builder()
                            .careerId(e.getKey())
                            .score(e.getValue())
                            .careerName(career.getName())
                            .build();
                })
                .toList();
    }
}
