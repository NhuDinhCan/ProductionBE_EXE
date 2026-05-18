package com.example.production.service;


import com.example.production.dto.UniversityRequestDTO;
import com.example.production.dto.UniversityResponseDTO;
import com.example.production.entity.UniversityMajor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.example.production.repositpry.UniversityMajorRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UniversityService {
    private final UniversityMajorRepository universityMajorRepository;

    public List<UniversityResponseDTO> suggestUniversity(UniversityRequestDTO request) {

        List<UniversityMajor> list =
                universityMajorRepository
                        .findByCareerIdAndScoreRequiredLessThanEqualOrderByScoreRequiredDesc(
                                request.getCareerId(),
                                request.getScore()
                        );

        return list.stream().map(
                u -> UniversityResponseDTO.builder()
                        .universityName(u.getUniversity().getName())
                        .careerName(u.getCareer().getName())
                        .scoreRequired(u.getScoreRequired())

                        .region(u.getUniversity().getRegion())
                        .type(u.getUniversity().getType())
                        .tuitionFee(u.getUniversity().getTuitionFee())

                        .build()
        ).toList();
    }
}
