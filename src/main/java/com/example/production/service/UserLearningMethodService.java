package com.example.production.service;

import com.example.production.dto.ApplyLearningMethodRequest;
import com.example.production.dto.UserLearningMethodResponse;
import com.example.production.entity.Career;
import com.example.production.entity.LearningMethod;
import com.example.production.entity.User;
import com.example.production.entity.UserLearningMethod;
import com.example.production.exception.AppException;
import com.example.production.repositpry.CareerRepository;
import com.example.production.repositpry.LearningMethodRepository;
import com.example.production.repositpry.MajorLearningMethodRepository;
import com.example.production.repositpry.UserLearningMethodRepository;
import com.example.production.repositpry.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserLearningMethodService {

    private static final String ACTIVE = "ACTIVE";

    private final UserLearningMethodRepository userLearningMethodRepository;
    private final UserRepository userRepository;
    private final CareerRepository careerRepository;
    private final LearningMethodRepository learningMethodRepository;
    private final MajorLearningMethodRepository majorLearningMethodRepository;
    private final StudyScheduleService studyScheduleService;

    @Transactional(readOnly = true)
    public List<UserLearningMethodResponse> getAppliedMethods(Jwt jwt, Long careerId) {
        User user = currentUser(jwt);
        List<UserLearningMethod> methods = careerId == null
                ? userLearningMethodRepository.findByUserIdAndStatusOrderByAppliedAtDesc(user.getId(), ACTIVE)
                : userLearningMethodRepository.findByUserIdAndCareerIdAndStatusOrderByAppliedAtDesc(user.getId(), careerId, ACTIVE);

        return methods.stream()
                .map(method -> toResponse(method, true))
                .toList();
    }

    @Transactional
    public UserLearningMethodResponse applyMethod(Jwt jwt, ApplyLearningMethodRequest request) {
        User user = currentUser(jwt);
        Career career = careerRepository.findById(request.getCareerId())
                .orElseThrow(() -> AppException.notFound("Ngành học không tồn tại"));
        LearningMethod method = learningMethodRepository.findById(request.getLearningMethodId())
                .orElseThrow(() -> AppException.notFound("Phương pháp học không tồn tại"));

        if (!majorLearningMethodRepository.existsByCareerIdAndLearningMethodId(career.getId(), method.getId())) {
            throw AppException.badRequest("Phương pháp học không thuộc ngành đã chọn");
        }

        var existing = userLearningMethodRepository
                .findByUserIdAndCareerIdAndLearningMethodId(user.getId(), career.getId(), method.getId());
        boolean alreadyApplied = existing.isPresent();
        UserLearningMethod applied = existing.orElseGet(() -> userLearningMethodRepository.save(UserLearningMethod.builder()
                .user(user)
                .career(career)
                .learningMethod(method)
                .status(ACTIVE)
                .build()));

        studyScheduleService.generateForUser(user, career, method);
        return toResponse(applied, alreadyApplied);
    }

    private User currentUser(Jwt jwt) {
        return userRepository.findByEmail(jwt.getSubject())
                .orElseThrow(() -> AppException.notFound("Người dùng không tồn tại"));
    }

    private UserLearningMethodResponse toResponse(UserLearningMethod applied, boolean alreadyApplied) {
        return UserLearningMethodResponse.builder()
                .id(applied.getId())
                .careerId(applied.getCareer().getId())
                .careerName(applied.getCareer().getName())
                .learningMethodId(applied.getLearningMethod().getId())
                .learningMethodTitle(applied.getLearningMethod().getTitle())
                .learningMethodDescription(applied.getLearningMethod().getDescription())
                .status(applied.getStatus())
                .appliedAt(applied.getAppliedAt())
                .alreadyApplied(alreadyApplied)
                .build();
    }
}


