package com.example.production.service;

import com.example.production.dto.LearningMethodDTO;
import com.example.production.dto.LearningStrategyResponse;
import com.example.production.dto.MajorDTO;
import com.example.production.entity.Career;
import com.example.production.entity.LearningMethod;
import com.example.production.entity.LearningStrategyProfile;
import com.example.production.exception.AppException;
import com.example.production.repositpry.CareerRepository;
import com.example.production.repositpry.LearningStrategyProfileRepository;
import com.example.production.repositpry.MajorLearningMethodRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LearningStrategyService {

    private static final String LIST_DELIMITER = "\\|";

    private final CareerRepository careerRepository;
    private final LearningStrategyProfileRepository profileRepository;
    private final MajorLearningMethodRepository majorLearningMethodRepository;

    @Transactional(readOnly = true)
    public List<MajorDTO> getMajors() {
        return careerRepository.findAll(Sort.by(Sort.Direction.ASC, "name"))
                .stream()
                .map(career -> MajorDTO.builder()
                        .code(String.valueOf(career.getId()))
                        .name(career.getName())
                        .build())
                .toList();
    }

    @Transactional(readOnly = true)
    public LearningStrategyResponse getStrategy(String major) {
        Career career = resolveCareer(major);
        LearningStrategyProfile profile = profileRepository.findByCareerId(career.getId())
                .orElseThrow(() -> AppException.notFound("Ngành học này chưa có cấu hình phương pháp học"));

        List<LearningMethodDTO> methods = majorLearningMethodRepository
                .findByCareerIdOrderBySortOrderAsc(career.getId())
                .stream()
                .map(mapping -> toMethodDTO(mapping.getLearningMethod()))
                .toList();

        if (methods.isEmpty()) {
            throw AppException.notFound("Ngành học này chưa có phương pháp học được cấu hình");
        }

        return LearningStrategyResponse.builder()
                .careerId(career.getId())
                .major(career.getName())
                .description(profile.getDescription())
                .methods(methods)
                .skills(splitList(profile.getSkills()))
                .tools(splitList(profile.getTools()))
                .weeklyRoadmap(splitList(profile.getWeeklyRoadmap()))
                .build();
    }

    private Career resolveCareer(String major) {
        if (!StringUtils.hasText(major)) {
            throw AppException.badRequest("Vui lòng chọn ngành học");
        }

        String value = major.trim();
        try {
            Long careerId = Long.valueOf(value);
            return careerRepository.findById(careerId)
                    .orElseThrow(() -> AppException.notFound("Ngành học không tồn tại"));
        } catch (NumberFormatException ignored) {
            return careerRepository.findFirstByNameIgnoreCase(value)
                    .orElseThrow(() -> AppException.notFound("Ngành học không tồn tại"));
        }
    }

    private LearningMethodDTO toMethodDTO(LearningMethod method) {
        return LearningMethodDTO.builder()
                .id(method.getId())
                .title(method.getTitle())
                .description(method.getDescription())
                .benefits(splitList(method.getBenefits()))
                .build();
    }

    private List<String> splitList(String rawValue) {
        if (!StringUtils.hasText(rawValue)) {
            return List.of();
        }

        return Arrays.stream(rawValue.split(LIST_DELIMITER))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .toList();
    }
}
