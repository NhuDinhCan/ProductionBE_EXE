package com.example.production.service;

import com.example.production.dto.ApplyLearningMethodRequest;
import com.example.production.dto.StudyScheduleRequest;
import com.example.production.dto.StudyScheduleResponse;
import com.example.production.entity.Career;
import com.example.production.entity.LearningMethod;
import com.example.production.entity.StudySchedule;
import com.example.production.entity.User;
import com.example.production.exception.AppException;
import com.example.production.repositpry.CareerRepository;
import com.example.production.repositpry.LearningMethodRepository;
import com.example.production.repositpry.MajorLearningMethodRepository;
import com.example.production.repositpry.StudyScheduleRepository;
import com.example.production.repositpry.UserLearningMethodRepository;
import com.example.production.repositpry.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StudyScheduleService {

    private static final String ACTIVE = "ACTIVE";
    private static final Map<String, Integer> DAY_ORDER = Map.of(
            "MONDAY", 1,
            "TUESDAY", 2,
            "WEDNESDAY", 3,
            "THURSDAY", 4,
            "FRIDAY", 5,
            "SATURDAY", 6,
            "SUNDAY", 7
    );
    private static final String GENERATED_DESCRIPTION_PREFIX = "Lịch học mẫu được tạo từ phương pháp ";

    private final StudyScheduleRepository studyScheduleRepository;
    private final UserRepository userRepository;
    private final CareerRepository careerRepository;
    private final LearningMethodRepository learningMethodRepository;
    private final MajorLearningMethodRepository majorLearningMethodRepository;
    private final UserLearningMethodRepository userLearningMethodRepository;

    @Transactional(readOnly = true)
    public List<StudyScheduleResponse> getSchedules(
            Jwt jwt,
            Long careerId,
            Long learningMethodId
    ) {
        User user = currentUser(jwt);
        if (learningMethodId != null && careerId == null) {
            throw AppException.badRequest("careerId is required when filtering by learningMethodId");
        }
        if (careerId != null) {
            getCareer(careerId);
        }
        if (careerId != null && learningMethodId != null) {
            validateMethodForCareer(careerId, learningMethodId);
        }

        List<StudySchedule> schedules;
        if (careerId != null && learningMethodId != null) {
            schedules = studyScheduleRepository
                    .findByUserIdAndCareerIdAndLearningMethodIdAndStatus(
                            user.getId(),
                            careerId,
                            learningMethodId,
                            ACTIVE
                    );
        } else if (careerId != null) {
            schedules = studyScheduleRepository
                    .findByUserIdAndCareerIdAndStatus(
                            user.getId(),
                            careerId,
                            ACTIVE
                    );
        } else {
            schedules = studyScheduleRepository
                    .findByUserIdAndStatusOrderByStartTimeAsc(
                            user.getId(),
                            ACTIVE
                    );
        }
        return schedules.stream()
                .sorted(scheduleComparator())
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<StudyScheduleResponse> getTodaySchedules(Jwt jwt) {
        User user = currentUser(jwt);
        String today = LocalDate.now().getDayOfWeek().name();
        return studyScheduleRepository.findByUserIdAndDayOfWeekAndStatusOrderByStartTimeAsc(
                        user.getId(), today, ACTIVE)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public List<StudyScheduleResponse> generateSchedules(Jwt jwt, ApplyLearningMethodRequest request) {
        User user = currentUser(jwt);
        Career career = getCareer(request.getCareerId());
        LearningMethod method = getLearningMethod(request.getLearningMethodId());
        validateMethodForCareer(career.getId(), method.getId());
        validateAppliedMethod(user.getId(), career.getId(), method.getId());
        return generateForUser(user, career, method).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public List<StudySchedule> generateForUser(User user, Career career, LearningMethod method) {
        List<StudySchedule> existing = studyScheduleRepository
                .findByUserIdAndCareerIdAndLearningMethodIdAndStatus(
                        user.getId(), career.getId(), method.getId(), ACTIVE);
        if (!existing.isEmpty()) {
            repairLegacyGeneratedText(existing);
            return existing.stream().sorted(scheduleComparator()).toList();
        }

        List<StudySchedule> schedules = List.of(
                buildGeneratedSchedule(user, career, method, "MONDAY", "19:30", "21:00", "Nắm nền tảng " + method.getTitle()),
                buildGeneratedSchedule(user, career, method, "WEDNESDAY", "19:30", "21:00", "Thực hành " + career.getName()),
                buildGeneratedSchedule(user, career, method, "FRIDAY", "20:00", "21:00", "Ôn tập và ghi chú lỗi sai"),
                buildGeneratedSchedule(user, career, method, "SUNDAY", "09:00", "10:30", "Tổng kết tuần và lên kế hoạch tiếp theo")
        );

        return studyScheduleRepository.saveAll(schedules);
    }

    @Transactional
    public StudyScheduleResponse createSchedule(Jwt jwt, StudyScheduleRequest request) {
        User user = currentUser(jwt);
        Career career = getCareer(request.getCareerId());
        LearningMethod method = resolveOptionalMethod(request.getLearningMethodId(), career.getId());
        validateScheduleRequest(request);

        StudySchedule schedule = StudySchedule.builder()
                .user(user)
                .career(career)
                .learningMethod(method)
                .title(request.getTitle().trim())
                .description(StringUtils.hasText(request.getDescription()) ? request.getDescription().trim() : null)
                .dayOfWeek(normalizeDay(request.getDayOfWeek()))
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .status(ACTIVE)
                .build();

        return toResponse(studyScheduleRepository.save(schedule));
    }

    @Transactional
    public StudyScheduleResponse updateSchedule(Jwt jwt, Long id, StudyScheduleRequest request) {
        User user = currentUser(jwt);
        StudySchedule schedule = getOwnedSchedule(id, user.getId());
        Career career = getCareer(request.getCareerId());
        LearningMethod method = resolveOptionalMethod(request.getLearningMethodId(), career.getId());
        validateScheduleRequest(request);

        schedule.setCareer(career);
        schedule.setLearningMethod(method);
        schedule.setTitle(request.getTitle().trim());
        schedule.setDescription(StringUtils.hasText(request.getDescription()) ? request.getDescription().trim() : null);
        schedule.setDayOfWeek(normalizeDay(request.getDayOfWeek()));
        schedule.setStartTime(request.getStartTime());
        schedule.setEndTime(request.getEndTime());

        return toResponse(studyScheduleRepository.save(schedule));
    }

    @Transactional
    public void deleteSchedule(Jwt jwt, Long id) {
        User user = currentUser(jwt);
        StudySchedule schedule = getOwnedSchedule(id, user.getId());
        studyScheduleRepository.delete(schedule);
    }

    private StudySchedule buildGeneratedSchedule(
            User user,
            Career career,
            LearningMethod method,
            String dayOfWeek,
            String startTime,
            String endTime,
            String title) {
        return StudySchedule.builder()
                .user(user)
                .career(career)
                .learningMethod(method)
                .title(title)
                .description(generatedDescription(method))
                .dayOfWeek(dayOfWeek)
                .startTime(LocalTime.parse(startTime))
                .endTime(LocalTime.parse(endTime))
                .status(ACTIVE)
                .build();
    }

    private void repairLegacyGeneratedText(List<StudySchedule> schedules) {
        boolean changed = false;
        for (StudySchedule schedule : schedules) {
            if (!isLegacyGeneratedSchedule(schedule)) {
                continue;
            }

            String generatedTitle = generatedTitle(schedule);
            if (generatedTitle != null && !generatedTitle.equals(schedule.getTitle())) {
                schedule.setTitle(generatedTitle);
                changed = true;
            }

            String generatedDescription = generatedDescription(schedule.getLearningMethod());
            if (!generatedDescription.equals(schedule.getDescription())) {
                schedule.setDescription(generatedDescription);
                changed = true;
            }
        }

        if (changed) {
            studyScheduleRepository.saveAll(schedules);
        }
    }

    private boolean isLegacyGeneratedSchedule(StudySchedule schedule) {
        return schedule.getLearningMethod() != null && hasLegacyMojibake(schedule.getDescription());
    }

    private boolean hasLegacyMojibake(String value) {
        return StringUtils.hasText(value)
                && (value.contains("Ã")
                || value.contains("Â")
                || value.contains("Æ")
                || value.contains("â")
                || value.contains("Ä"));
    }

    private String generatedTitle(StudySchedule schedule) {
        return switch (schedule.getDayOfWeek()) {
            case "MONDAY" -> "Nắm nền tảng " + schedule.getLearningMethod().getTitle();
            case "WEDNESDAY" -> "Thực hành " + schedule.getCareer().getName();
            case "FRIDAY" -> "Ôn tập và ghi chú lỗi sai";
            case "SUNDAY" -> "Tổng kết tuần và lên kế hoạch tiếp theo";
            default -> null;
        };
    }

    private String titleForResponse(StudySchedule schedule) {
        if (isLegacyGeneratedSchedule(schedule)) {
            String generatedTitle = generatedTitle(schedule);
            if (generatedTitle != null) {
                return generatedTitle;
            }
        }
        return schedule.getTitle();
    }

    private String descriptionForResponse(StudySchedule schedule) {
        if (isLegacyGeneratedSchedule(schedule)) {
            return generatedDescription(schedule.getLearningMethod());
        }
        return schedule.getDescription();
    }

    private String generatedDescription(LearningMethod method) {
        return GENERATED_DESCRIPTION_PREFIX + method.getTitle();
    }

    private User currentUser(Jwt jwt) {
        return userRepository.findByEmail(jwt.getSubject())
                .orElseThrow(() -> AppException.notFound("Người dùng không tồn tại"));
    }

    private Career getCareer(Long careerId) {
        return careerRepository.findById(careerId)
                .orElseThrow(() -> AppException.notFound("Ngành học không tồn tại"));
    }

    private LearningMethod getLearningMethod(Long learningMethodId) {
        return learningMethodRepository.findById(learningMethodId)
                .orElseThrow(() -> AppException.notFound("Phương pháp học không tồn tại"));
    }

    private LearningMethod resolveOptionalMethod(Long learningMethodId, Long careerId) {
        if (learningMethodId == null) {
            return null;
        }
        LearningMethod method = getLearningMethod(learningMethodId);
        validateMethodForCareer(careerId, learningMethodId);
        return method;
    }

    private void validateMethodForCareer(Long careerId, Long learningMethodId) {
        if (!majorLearningMethodRepository.existsByCareerIdAndLearningMethodId(careerId, learningMethodId)) {
            throw AppException.badRequest("Phương pháp học không thuộc ngành đã chọn");
        }
    }

    private void validateAppliedMethod(Long userId, Long careerId, Long learningMethodId) {
        if (!userLearningMethodRepository.existsByUserIdAndCareerIdAndLearningMethodIdAndStatus(
                userId,
                careerId,
                learningMethodId,
                ACTIVE)) {
            throw AppException.badRequest("Bạn chưa áp dụng phương pháp học này");
        }
    }

    private void validateScheduleRequest(StudyScheduleRequest request) {
        normalizeDay(request.getDayOfWeek());
        if (!request.getEndTime().isAfter(request.getStartTime())) {
            throw AppException.badRequest("endTime phải sau startTime");
        }
    }

    private StudySchedule getOwnedSchedule(Long id, Long userId) {
        return studyScheduleRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> AppException.notFound("Lịch học không tồn tại"));
    }

    private String normalizeDay(String dayOfWeek) {
        if (!StringUtils.hasText(dayOfWeek)) {
            throw AppException.badRequest("dayOfWeek không được để trống");
        }
        try {
            return DayOfWeek.valueOf(dayOfWeek.trim().toUpperCase()).name();
        } catch (IllegalArgumentException ex) {
            throw AppException.badRequest("dayOfWeek không hợp lệ");
        }
    }

    private Comparator<StudySchedule> scheduleComparator() {
        return Comparator
                .comparing((StudySchedule s) -> DAY_ORDER.getOrDefault(s.getDayOfWeek(), 99))
                .thenComparing(StudySchedule::getStartTime, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(StudySchedule::getId);
    }

    private StudyScheduleResponse toResponse(StudySchedule schedule) {
        LearningMethod method = schedule.getLearningMethod();
        return StudyScheduleResponse.builder()
                .id(schedule.getId())
                .careerId(schedule.getCareer().getId())
                .careerName(schedule.getCareer().getName())
                .learningMethodId(method != null ? method.getId() : null)
                .learningMethodTitle(method != null ? method.getTitle() : null)
                .title(titleForResponse(schedule))
                .description(descriptionForResponse(schedule))
                .dayOfWeek(schedule.getDayOfWeek())
                .startTime(schedule.getStartTime())
                .endTime(schedule.getEndTime())
                .status(schedule.getStatus())
                .createdAt(schedule.getCreatedAt())
                .build();
    }
}

