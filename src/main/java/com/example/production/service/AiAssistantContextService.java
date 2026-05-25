package com.example.production.service;

import com.example.production.entity.Career;
import com.example.production.entity.LearningMethod;
import com.example.production.entity.LearningResource;
import com.example.production.entity.LearningStrategyProfile;
import com.example.production.entity.MajorLearningMethod;
import com.example.production.entity.MockExamAnswer;
import com.example.production.entity.MockExamAttempt;
import com.example.production.entity.StudySchedule;
import com.example.production.entity.UniversityMajor;
import com.example.production.entity.User;
import com.example.production.entity.UserLearningMethod;
import com.example.production.entity.UserSavedResource;
import com.example.production.exception.AppException;
import com.example.production.repositpry.CareerRepository;
import com.example.production.repositpry.LearningMethodRepository;
import com.example.production.repositpry.LearningResourceRepository;
import com.example.production.repositpry.LearningStrategyProfileRepository;
import com.example.production.repositpry.MajorLearningMethodRepository;
import com.example.production.repositpry.MockExamAnswerRepository;
import com.example.production.repositpry.MockExamAttemptRepository;
import com.example.production.repositpry.StudyScheduleRepository;
import com.example.production.repositpry.UniversityMajorRepository;
import com.example.production.repositpry.UserLearningMethodRepository;
import com.example.production.repositpry.UserRepository;
import com.example.production.repositpry.UserSavedResourceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.text.Normalizer;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AiAssistantContextService {

    private static final int MAX_TEXT_LENGTH = 280;

    private final UserRepository userRepository;
    private final CareerRepository careerRepository;
    private final LearningStrategyProfileRepository learningStrategyProfileRepository;
    private final LearningMethodRepository learningMethodRepository;
    private final MajorLearningMethodRepository majorLearningMethodRepository;
    private final StudyScheduleRepository studyScheduleRepository;
    private final LearningResourceRepository learningResourceRepository;
    private final UserSavedResourceRepository userSavedResourceRepository;
    private final UserLearningMethodRepository userLearningMethodRepository;
    private final MockExamAttemptRepository mockExamAttemptRepository;
    private final MockExamAnswerRepository mockExamAnswerRepository;
    private final UniversityMajorRepository universityMajorRepository;

    @Transactional(readOnly = true)
    public String buildContext(Jwt jwt, String message, String contextType) {
        User user = userRepository.findByEmail(jwt.getSubject())
                .orElseThrow(() -> AppException.notFound("Không tìm thấy user"));

        Long userId = user.getId();
        List<Career> careers = careerRepository.findAll().stream()
                .sorted(Comparator.comparing(Career::getName, Comparator.nullsLast(String::compareToIgnoreCase)))
                .toList();
        List<UserLearningMethod> userMethods =
                userLearningMethodRepository.findByUserIdAndStatusOrderByAppliedAtDesc(userId, "ACTIVE");
        List<StudySchedule> schedules =
                studyScheduleRepository.findByUserIdAndStatusOrderByStartTimeAsc(userId, "ACTIVE");
        List<UserSavedResource> savedResources =
                userSavedResourceRepository.findByUserIdOrderBySavedAtDesc(userId);

        Career targetCareer = chooseTargetCareer(message, careers, userMethods, schedules, savedResources);

        StringBuilder context = new StringBuilder();
        context.append("contextType: ").append(blankToDefault(contextType, "GENERAL")).append('\n');
        context.append("Người dùng hiện tại đã đăng nhập. Không có email, password, token hoặc refresh token trong context này.\n");

        appendCareers(context, careers, targetCareer);
        appendLearningStrategy(context, targetCareer);
        appendLearningMethods(context, targetCareer, userMethods);
        appendStudySchedules(context, schedules);
        appendLearningResources(context, targetCareer, savedResources);
        appendMockExamAttempts(context, userId);
        appendQuizStatus(context);
        appendUniversityMajors(context, targetCareer);

        return context.toString();
    }

    private Career chooseTargetCareer(
            String message,
            List<Career> careers,
            List<UserLearningMethod> userMethods,
            List<StudySchedule> schedules,
            List<UserSavedResource> savedResources) {
        String normalizedMessage = normalize(message);

        Optional<Career> mentionedCareer = careers.stream()
                .filter(career -> StringUtils.hasText(career.getName()))
                .filter(career -> normalizedMessage.contains(normalize(career.getName())))
                .findFirst();
        if (mentionedCareer.isPresent()) {
            return mentionedCareer.get();
        }

        if (!userMethods.isEmpty()) {
            return userMethods.getFirst().getCareer();
        }
        if (!schedules.isEmpty()) {
            return schedules.getFirst().getCareer();
        }
        if (!savedResources.isEmpty()) {
            return savedResources.getFirst().getResource().getCareer();
        }
        return null;
    }

    private void appendCareers(StringBuilder context, List<Career> careers, Career targetCareer) {
        context.append("\n[Ngành học]\n");
        if (targetCareer != null) {
            context.append("- Ngành đang quan tâm/suy luận từ dữ liệu: ")
                    .append(safe(targetCareer.getName()))
                    .append(" (id=").append(targetCareer.getId()).append(")\n");
        } else {
            context.append("- Chưa có ngành đang quan tâm được lưu/suy luận rõ ràng.\n");
        }

        if (careers.isEmpty()) {
            context.append("- Chưa có dữ liệu ngành học trong hệ thống.\n");
            return;
        }

        context.append("- Danh sách ngành có trong hệ thống: ");
        context.append(careers.stream()
                .limit(20)
                .map(Career::getName)
                .filter(StringUtils::hasText)
                .map(this::safe)
                .toList());
        context.append('\n');
    }

    private void appendLearningStrategy(StringBuilder context, Career targetCareer) {
        context.append("\n[Chiến lược học tập theo ngành]\n");
        if (targetCareer == null) {
            context.append("- Chưa có ngành mục tiêu để lấy learning_strategy_profiles.\n");
            return;
        }

        learningStrategyProfileRepository.findByCareerId(targetCareer.getId())
                .ifPresentOrElse(profile -> appendProfile(context, profile),
                        () -> context.append("- Chưa có learning_strategy_profiles cho ngành ")
                                .append(safe(targetCareer.getName()))
                                .append(".\n"));
    }

    private void appendProfile(StringBuilder context, LearningStrategyProfile profile) {
        context.append("- Mô tả: ").append(safe(profile.getDescription())).append('\n');
        context.append("- Kỹ năng: ").append(safe(profile.getSkills())).append('\n');
        context.append("- Công cụ: ").append(safe(profile.getTools())).append('\n');
        context.append("- Roadmap tuần: ").append(safe(profile.getWeeklyRoadmap())).append('\n');
    }

    private void appendLearningMethods(
            StringBuilder context,
            Career targetCareer,
            List<UserLearningMethod> userMethods) {
        context.append("\n[Phương pháp học]\n");
        if (userMethods.isEmpty()) {
            context.append("- User chưa áp dụng phương pháp học nào.\n");
        } else {
            userMethods.stream().limit(6).forEach(item -> context.append("- Đã áp dụng: ")
                    .append(safe(item.getLearningMethod().getTitle()))
                    .append(" cho ngành ")
                    .append(safe(item.getCareer().getName()))
                    .append(", trạng thái ")
                    .append(safe(item.getStatus()))
                    .append('\n'));
        }

        if (targetCareer != null) {
            List<MajorLearningMethod> methods =
                    majorLearningMethodRepository.findByCareerIdOrderBySortOrderAsc(targetCareer.getId());
            if (methods.isEmpty()) {
                context.append("- Chưa có major_learning_methods cho ngành ")
                        .append(safe(targetCareer.getName()))
                        .append(".\n");
            } else {
                methods.stream().limit(8).forEach(item -> context.append("- Gợi ý theo ngành: ")
                        .append(safe(item.getLearningMethod().getTitle()))
                        .append(" - ")
                        .append(safe(item.getLearningMethod().getDescription()))
                        .append('\n'));
            }
        }

        List<LearningMethod> allMethods = learningMethodRepository.findAll().stream()
                .sorted(Comparator.comparing(LearningMethod::getTitle, Comparator.nullsLast(String::compareToIgnoreCase)))
                .limit(10)
                .toList();
        if (!allMethods.isEmpty()) {
            context.append("- Các phương pháp có trong hệ thống: ")
                    .append(allMethods.stream().map(LearningMethod::getTitle).map(this::safe).toList())
                    .append('\n');
        }
    }

    private void appendStudySchedules(StringBuilder context, List<StudySchedule> schedules) {
        context.append("\n[Lịch học hiện tại]\n");
        if (schedules.isEmpty()) {
            context.append("- User chưa có lịch học ACTIVE.\n");
            return;
        }

        schedules.stream().limit(10).forEach(schedule -> context.append("- ")
                .append(safe(schedule.getDayOfWeek()))
                .append(" ")
                .append(schedule.getStartTime() != null ? schedule.getStartTime() : "")
                .append("-")
                .append(schedule.getEndTime() != null ? schedule.getEndTime() : "")
                .append(": ")
                .append(safe(schedule.getTitle()))
                .append(" | ngành ")
                .append(safe(schedule.getCareer().getName()))
                .append(" | phương pháp ")
                .append(schedule.getLearningMethod() != null ? safe(schedule.getLearningMethod().getTitle()) : "chưa gắn")
                .append('\n'));
    }

    private void appendLearningResources(
            StringBuilder context,
            Career targetCareer,
            List<UserSavedResource> savedResources) {
        context.append("\n[Tài liệu học]\n");
        if (savedResources.isEmpty()) {
            context.append("- User chưa lưu tài liệu nào.\n");
        } else {
            savedResources.stream().limit(8).forEach(saved -> appendResourceLine(context, "Đã lưu", saved.getResource()));
        }

        if (targetCareer == null) {
            context.append("- Chưa có ngành mục tiêu để gợi ý learning_resources theo ngành.\n");
            return;
        }

        List<LearningResource> resources = learningResourceRepository.search("ACTIVE", targetCareer.getId(), null, null)
                .stream()
                .limit(8)
                .toList();
        if (resources.isEmpty()) {
            context.append("- Chưa có learning_resources ACTIVE cho ngành ")
                    .append(safe(targetCareer.getName()))
                    .append(".\n");
        } else {
            resources.forEach(resource -> appendResourceLine(context, "Theo ngành", resource));
        }
    }

    private void appendResourceLine(StringBuilder context, String prefix, LearningResource resource) {
        context.append("- ")
                .append(prefix)
                .append(": ")
                .append(safe(resource.getTitle()))
                .append(" | ")
                .append(safe(resource.getResourceType()))
                .append(" | level ")
                .append(safe(resource.getLevel()))
                .append(" | ngành ")
                .append(safe(resource.getCareer().getName()))
                .append('\n');
    }

    private void appendMockExamAttempts(StringBuilder context, Long userId) {
        context.append("\n[Kết quả luyện đề gần đây]\n");
        List<MockExamAttempt> attempts = mockExamAttemptRepository.findByUserIdOrderByStartedAtDesc(userId)
                .stream()
                .limit(5)
                .toList();
        if (attempts.isEmpty()) {
            context.append("- User chưa có lịch sử làm đề thi thử.\n");
            return;
        }

        attempts.forEach(attempt -> context.append("- ")
                .append(safe(attempt.getExam().getTitle()))
                .append(" | môn ")
                .append(safe(attempt.getExam().getSubject().getName()))
                .append(" | trạng thái ")
                .append(safe(attempt.getStatus()))
                .append(" | điểm ")
                .append(attempt.getScore())
                .append(" | đúng/sai/chưa trả lời ")
                .append(attempt.getCorrectCount())
                .append("/")
                .append(attempt.getWrongCount())
                .append("/")
                .append(attempt.getUnansweredCount())
                .append('\n'));

        attempts.stream()
                .filter(attempt -> "SUBMITTED".equals(attempt.getStatus()) || "EXPIRED".equals(attempt.getStatus()))
                .findFirst()
                .ifPresent(attempt -> appendRecentWrongAnswers(context, attempt));
    }

    private void appendRecentWrongAnswers(StringBuilder context, MockExamAttempt attempt) {
        List<MockExamAnswer> answers = mockExamAnswerRepository.findByAttemptIdWithQuestions(attempt.getId())
                .stream()
                .filter(answer -> !Boolean.TRUE.equals(answer.getCorrect()))
                .limit(6)
                .toList();
        if (answers.isEmpty()) {
            context.append("- Không có câu sai/chưa đúng trong lần làm gần nhất đã nộp.\n");
            return;
        }

        context.append("- Câu cần ôn từ lần làm gần nhất:\n");
        answers.forEach(answer -> context.append("  + ")
                .append(safe(answer.getQuestion().getQuestionText()))
                .append(" | chọn ")
                .append(safe(answer.getSelectedOption()))
                .append(" | đáp án ")
                .append(safe(answer.getQuestion().getCorrectOption()))
                .append(" | giải thích ")
                .append(safe(answer.getQuestion().getExplanation()))
                .append('\n'));
    }

    private void appendQuizStatus(StringBuilder context) {
        context.append("\n[Kết quả quiz định hướng]\n");
        context.append("- Hệ thống hiện chưa lưu lịch sử/kết quả quiz định hướng theo user, nên không có dữ liệu quiz cá nhân để gửi cho AI.\n");
    }

    private void appendUniversityMajors(StringBuilder context, Career targetCareer) {
        context.append("\n[Trường/ngành xét tuyển]\n");
        if (targetCareer == null) {
            context.append("- Chưa có ngành mục tiêu để lấy university_major.\n");
            return;
        }

        List<UniversityMajor> majors =
                universityMajorRepository.findTop8ByCareerIdOrderByScoreRequiredDesc(targetCareer.getId());
        if (majors.isEmpty()) {
            context.append("- Chưa có university_major cho ngành ")
                    .append(safe(targetCareer.getName()))
                    .append(".\n");
            return;
        }

        majors.forEach(major -> context.append("- ")
                .append(major.getUniversity() != null ? safe(major.getUniversity().getName()) : "Không rõ trường")
                .append(" | ngành ")
                .append(major.getCareer() != null ? safe(major.getCareer().getName()) : safe(targetCareer.getName()))
                .append(" | điểm chuẩn ")
                .append(major.getScoreRequired())
                .append('\n'));
    }

    private String blankToDefault(String value, String defaultValue) {
        return StringUtils.hasText(value) ? value.trim() : defaultValue;
    }

    private String safe(String value) {
        if (!StringUtils.hasText(value)) {
            return "chưa có dữ liệu";
        }
        String normalized = value.replaceAll("\\s+", " ").trim();
        if (normalized.length() <= MAX_TEXT_LENGTH) {
            return normalized;
        }
        return normalized.substring(0, MAX_TEXT_LENGTH) + "...";
    }

    private String normalize(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }
        String lower = value.toLowerCase(Locale.ROOT);
        String withoutAccents = Normalizer.normalize(lower, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return withoutAccents.replace('đ', 'd');
    }
}
