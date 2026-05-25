package com.example.production.service;

import com.example.production.dto.MockExamAnswerResultResponse;
import com.example.production.dto.MockExamAttemptDetailResponse;
import com.example.production.dto.MockExamAttemptSummaryResponse;
import com.example.production.dto.MockExamCombinationResponse;
import com.example.production.dto.MockExamQuestionResponse;
import com.example.production.dto.MockExamResponse;
import com.example.production.dto.MockExamStartResponse;
import com.example.production.dto.MockExamSubjectResponse;
import com.example.production.dto.MockExamSubmitRequest;
import com.example.production.entity.ExamCombination;
import com.example.production.entity.ExamCombinationSubject;
import com.example.production.entity.ExamSubject;
import com.example.production.entity.MockExam;
import com.example.production.entity.MockExamAnswer;
import com.example.production.entity.MockExamAttempt;
import com.example.production.entity.MockQuestion;
import com.example.production.entity.User;
import com.example.production.exception.AppException;
import com.example.production.repositpry.ExamCombinationRepository;
import com.example.production.repositpry.ExamCombinationSubjectRepository;
import com.example.production.repositpry.ExamSubjectRepository;
import com.example.production.repositpry.MockExamAnswerRepository;
import com.example.production.repositpry.MockExamAttemptRepository;
import com.example.production.repositpry.MockExamRepository;
import com.example.production.repositpry.MockQuestionRepository;
import com.example.production.repositpry.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MockExamService {

    private static final String IN_PROGRESS = "IN_PROGRESS";
    private static final String SUBMITTED = "SUBMITTED";
    private static final String EXPIRED = "EXPIRED";
    private static final String ACTIVE = "ACTIVE";
    private static final String DRAFT = "DRAFT";
    private static final String HIDDEN = "HIDDEN";
    private static final Set<String> ALLOWED_OPTIONS = Set.of("A", "B", "C", "D");

    private final ExamSubjectRepository examSubjectRepository;
    private final ExamCombinationRepository examCombinationRepository;
    private final ExamCombinationSubjectRepository examCombinationSubjectRepository;
    private final MockExamRepository mockExamRepository;
    private final MockQuestionRepository mockQuestionRepository;
    private final MockExamAttemptRepository mockExamAttemptRepository;
    private final MockExamAnswerRepository mockExamAnswerRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<MockExamSubjectResponse> getSubjects() {
        return examSubjectRepository.findAllByOrderByNameAsc().stream()
                .map(this::toSubjectResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MockExamCombinationResponse> getCombinations() {
        return examCombinationRepository.findAllByOrderByCodeAsc().stream()
                .map(this::toCombinationResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MockExamResponse> getExams(
            Long subjectId,
            String combinationCode,
            String difficulty,
            Integer year,
            String keyword) {
        String normalizedCombination = normalizeOptionalUpper(combinationCode);
        String normalizedDifficulty = normalizeOptionalUpper(difficulty);
        String normalizedKeyword = StringUtils.hasText(keyword) ? keyword.trim() : null;

        return mockExamRepository.search(
                        subjectId,
                        normalizedCombination,
                        normalizedDifficulty,
                        year,
                        ACTIVE,
                        normalizedKeyword)
                .stream()
                .map(exam -> toExamResponse(exam, false, false))
                .toList();
    }

    @Transactional(readOnly = true)
    public MockExamResponse getExam(Long examId) {
        MockExam exam = getExamOrThrow(examId);
        ensureActiveExam(exam);
        return toExamResponse(exam, true, false);
    }

    @Transactional
    public MockExamStartResponse startExam(Jwt jwt, Long examId) {
        User user = currentUser(jwt);
        MockExam exam = getExamOrThrow(examId);
        ensureStartableExam(jwt, user, exam);
        List<MockQuestion> questions = mockQuestionRepository.findByExamIdOrderByOrderIndexAscIdAsc(exam.getId());

        MockExamAttempt attempt = mockExamAttemptRepository.save(MockExamAttempt.builder()
                .user(user)
                .exam(exam)
                .status(IN_PROGRESS)
                .startedAt(LocalDateTime.now())
                .score(0.0)
                .totalQuestions(questions.size())
                .correctCount(0)
                .wrongCount(0)
                .unansweredCount(questions.size())
                .build());

        return MockExamStartResponse.builder()
                .attemptId(attempt.getId())
                .status(attempt.getStatus())
                .startedAt(attempt.getStartedAt())
                .remainingSeconds(remainingSeconds(attempt))
                .exam(toExamResponse(exam, questions, false))
                .selectedAnswers(List.of())
                .build();
    }

    @Transactional(readOnly = true)
    public MockExamStartResponse resumeAttempt(Jwt jwt, Long attemptId) {
        User user = currentUser(jwt);
        MockExamAttempt attempt = getOwnedAttempt(user.getId(), attemptId);

        if (!IN_PROGRESS.equals(attempt.getStatus())) {
            throw AppException.badRequest("Chỉ có thể tiếp tục bài thi đang làm");
        }

        MockExam exam = attempt.getExam();
        List<MockQuestion> questions = mockQuestionRepository.findByExamIdOrderByOrderIndexAscIdAsc(exam.getId());
        List<MockExamAnswer> answers = mockExamAnswerRepository.findByAttemptIdWithQuestions(attempt.getId());

        return MockExamStartResponse.builder()
                .attemptId(attempt.getId())
                .status(attempt.getStatus())
                .startedAt(attempt.getStartedAt())
                .remainingSeconds(remainingSeconds(attempt))
                .exam(toExamResponse(exam, questions, false))
                .selectedAnswers(toSelectedAnswers(answers))
                .build();
    }

    @Transactional
    public MockExamAttemptDetailResponse submitAttempt(Jwt jwt, Long attemptId, MockExamSubmitRequest request) {
        User user = currentUser(jwt);
        MockExamAttempt attempt = getOwnedAttempt(user.getId(), attemptId);

        if (SUBMITTED.equals(attempt.getStatus())) {
            throw AppException.conflict("Bài thi này đã được nộp");
        }
        if (EXPIRED.equals(attempt.getStatus())) {
            throw AppException.conflict("Bài thi này đã hết hạn");
        }

        List<MockQuestion> questions = mockQuestionRepository.findByExamIdOrderByOrderIndexAscIdAsc(attempt.getExam().getId());
        Map<Long, String> selectedByQuestionId = normalizeAnswers(request);
        validateQuestionOwnership(questions, selectedByQuestionId.keySet());

        LocalDateTime now = LocalDateTime.now();
        List<MockExamAnswer> answers = new ArrayList<>();
        int correctCount = 0;
        int unansweredCount = 0;

        for (MockQuestion question : questions) {
            String selectedOption = selectedByQuestionId.get(question.getId());

            if (!StringUtils.hasText(selectedOption)) {
                unansweredCount++;
                continue;
            }

            boolean correct = normalizeRequiredOption(question.getCorrectOption()).equals(selectedOption);
            if (correct) {
                correctCount++;
            }

            answers.add(MockExamAnswer.builder()
                    .attempt(attempt)
                    .question(question)
                    .selectedOption(selectedOption)
                    .correct(correct)
                    .answeredAt(now)
                    .build());
        }

        int totalQuestions = questions.size();
        int wrongCount = totalQuestions - correctCount - unansweredCount;
        attempt.setStatus(isExpired(attempt, now) ? EXPIRED : SUBMITTED);
        attempt.setSubmittedAt(now);
        attempt.setDurationSeconds(calculateDurationSeconds(attempt.getStartedAt(), now));
        attempt.setTotalQuestions(totalQuestions);
        attempt.setCorrectCount(correctCount);
        attempt.setWrongCount(Math.max(wrongCount, 0));
        attempt.setUnansweredCount(unansweredCount);
        attempt.setScore(calculateScore(correctCount, totalQuestions));

        mockExamAttemptRepository.save(attempt);
        mockExamAnswerRepository.saveAll(answers);

        return toAttemptDetail(attempt, questions, answers, true);
    }

    @Transactional(readOnly = true)
    public List<MockExamAttemptSummaryResponse> getAttempts(Jwt jwt) {
        User user = currentUser(jwt);
        return mockExamAttemptRepository.findByUserIdOrderByStartedAtDesc(user.getId()).stream()
                .map(this::toAttemptSummary)
                .toList();
    }

    @Transactional(readOnly = true)
    public MockExamAttemptDetailResponse getAttempt(Jwt jwt, Long attemptId) {
        User user = currentUser(jwt);
        MockExamAttempt attempt = getOwnedAttempt(user.getId(), attemptId);
        List<MockQuestion> questions = mockQuestionRepository.findByExamIdOrderByOrderIndexAscIdAsc(attempt.getExam().getId());
        List<MockExamAnswer> answers = mockExamAnswerRepository.findByAttemptIdWithQuestions(attempt.getId());
        boolean includeCorrect = !IN_PROGRESS.equals(attempt.getStatus());
        return toAttemptDetail(attempt, questions, answers, includeCorrect);
    }

    @Transactional
    public void deleteAttempt(Jwt jwt, Long attemptId) {
        User user = currentUser(jwt);
        MockExamAttempt attempt = getOwnedAttempt(user.getId(), attemptId);
        mockExamAnswerRepository.deleteByAttemptId(attempt.getId());
        mockExamAttemptRepository.delete(attempt);
    }

    @Transactional
    public void deleteAllAttempts(Jwt jwt) {
        User user = currentUser(jwt);
        mockExamAnswerRepository.deleteByAttemptUserId(user.getId());
        mockExamAttemptRepository.deleteByUserId(user.getId());
    }

    private void ensureActiveExam(MockExam exam) {
        if (exam == null) {
            throw AppException.notFound("Không tìm thấy đề thi");
        }
        if (!ACTIVE.equalsIgnoreCase(exam.getStatus())) {
            throw AppException.badRequest("Đề thi chưa được publish hoặc đã bị ẩn");
        }
    }

    private void ensureStartableExam(Jwt jwt, User user, MockExam exam) {
        if (exam == null) {
            throw AppException.notFound("Không tìm thấy đề thi");
        }

        String status = normalizeOptionalUpper(exam.getStatus());
        if (ACTIVE.equals(status)) {
            return;
        }
        if (HIDDEN.equals(status)) {
            throw AppException.badRequest("Đề thi đã bị ẩn, không thể làm lại");
        }

        boolean userHasPreviousAttempt = user != null
                && user.getId() != null
                && mockExamAttemptRepository.existsByUserIdAndExamId(user.getId(), exam.getId());

        if ((DRAFT.equals(status) || status == null) && (isAdmin(jwt) || userHasPreviousAttempt)) {
            return;
        }

        throw AppException.badRequest("Đề thi chưa được publish hoặc đã bị ẩn");
    }

    private MockExam getExamOrThrow(Long examId) {
        return mockExamRepository.findById(examId)
                .orElseThrow(() -> AppException.notFound("Không tìm thấy đề thi"));
    }

    private MockExamAttempt getOwnedAttempt(Long userId, Long attemptId) {
        return mockExamAttemptRepository.findByIdAndUserId(attemptId, userId)
                .orElseThrow(() -> AppException.notFound("Không tìm thấy bài làm"));
    }

    private User currentUser(Jwt jwt) {
        if (jwt == null || !StringUtils.hasText(jwt.getSubject())) {
            throw AppException.unauthorized("Vui lòng đăng nhập");
        }

        return userRepository.findByEmail(jwt.getSubject())
                .orElseThrow(() -> AppException.notFound("Người dùng không tồn tại"));
    }

    private boolean isAdmin(Jwt jwt) {
        return jwt != null
                && jwt.getClaimAsStringList("authorities") != null
                && jwt.getClaimAsStringList("authorities").contains("ADMIN");
    }

    private MockExamSubjectResponse toSubjectResponse(ExamSubject subject) {
        return MockExamSubjectResponse.builder()
                .id(subject.getId())
                .code(subject.getCode())
                .name(subject.getName())
                .build();
    }

    private MockExamCombinationResponse toCombinationResponse(ExamCombination combination) {
        List<MockExamSubjectResponse> subjects = examCombinationSubjectRepository
                .findByCombinationIdWithSubject(combination.getId())
                .stream()
                .map(ExamCombinationSubject::getSubject)
                .map(this::toSubjectResponse)
                .toList();

        return MockExamCombinationResponse.builder()
                .id(combination.getId())
                .code(combination.getCode())
                .name(combination.getName())
                .subjects(subjects)
                .build();
    }

    private MockExamResponse toExamResponse(MockExam exam, boolean includeQuestions, boolean includeCorrect) {
        List<MockQuestion> questions = includeQuestions
                ? mockQuestionRepository.findByExamIdOrderByOrderIndexAscIdAsc(exam.getId())
                : List.of();

        return toExamResponse(exam, questions, includeCorrect);
    }

    private MockExamResponse toExamResponse(MockExam exam, List<MockQuestion> questions, boolean includeCorrect) {
        Integer totalQuestions = questions.isEmpty()
                ? Math.toIntExact(mockQuestionRepository.countByExamId(exam.getId()))
                : questions.size();

        return MockExamResponse.builder()
                .id(exam.getId())
                .title(exam.getTitle())
                .subjectId(exam.getSubject().getId())
                .subjectCode(exam.getSubject().getCode())
                .subjectName(exam.getSubject().getName())
                .combinationId(exam.getCombination() != null ? exam.getCombination().getId() : null)
                .combinationCode(exam.getCombination() != null ? exam.getCombination().getCode() : null)
                .combinationName(exam.getCombination() != null ? exam.getCombination().getName() : null)
                .difficulty(exam.getDifficulty())
                .year(exam.getYear())
                .durationMinutes(exam.getDurationMinutes())
                .totalQuestions(totalQuestions)
                .questions(questions.isEmpty()
                        ? null
                        : questions.stream().map(question -> toQuestionResponse(question, includeCorrect)).toList())
                .build();
    }

    private MockExamQuestionResponse toQuestionResponse(MockQuestion question, boolean includeCorrect) {
        return MockExamQuestionResponse.builder()
                .id(question.getId())
                .orderIndex(question.getOrderIndex())
                .questionText(question.getQuestionText())
                .imageUrl(question.getImageUrl())
                .optionA(question.getOptionA())
                .optionB(question.getOptionB())
                .optionC(question.getOptionC())
                .optionD(question.getOptionD())
                .correctOption(includeCorrect ? normalizeRequiredOption(question.getCorrectOption()) : null)
                .explanation(includeCorrect ? question.getExplanation() : null)
                .build();
    }

    private MockExamAttemptSummaryResponse toAttemptSummary(MockExamAttempt attempt) {
        MockExam exam = attempt.getExam();
        ExamCombination combination = exam.getCombination();

        return MockExamAttemptSummaryResponse.builder()
                .id(attempt.getId())
                .examId(exam.getId())
                .examTitle(exam.getTitle())
                .subjectName(exam.getSubject().getName())
                .combinationCode(combination != null ? combination.getCode() : null)
                .status(attempt.getStatus())
                .startedAt(attempt.getStartedAt())
                .submittedAt(attempt.getSubmittedAt())
                .durationSeconds(attempt.getDurationSeconds())
                .score(attempt.getScore())
                .totalQuestions(attempt.getTotalQuestions())
                .correctCount(attempt.getCorrectCount())
                .wrongCount(attempt.getWrongCount())
                .unansweredCount(attempt.getUnansweredCount())
                .build();
    }

    private MockExamAttemptDetailResponse toAttemptDetail(
            MockExamAttempt attempt,
            List<MockQuestion> questions,
            List<MockExamAnswer> answers,
            boolean includeCorrect) {
        Map<Long, MockExamAnswer> answerByQuestionId = answers.stream()
                .collect(Collectors.toMap(
                        answer -> answer.getQuestion().getId(),
                        Function.identity(),
                        (left, right) -> right,
                        HashMap::new));

        List<MockExamAnswerResultResponse> answerResponses = questions.stream()
                .sorted(Comparator
                        .comparing((MockQuestion question) -> question.getOrderIndex() == null ? Integer.MAX_VALUE : question.getOrderIndex())
                        .thenComparing(MockQuestion::getId))
                .map(question -> toAnswerResult(question, answerByQuestionId.get(question.getId()), includeCorrect))
                .toList();

        return MockExamAttemptDetailResponse.builder()
                .id(attempt.getId())
                .examId(attempt.getExam().getId())
                .examTitle(attempt.getExam().getTitle())
                .subjectName(attempt.getExam().getSubject().getName())
                .combinationCode(attempt.getExam().getCombination() != null ? attempt.getExam().getCombination().getCode() : null)
                .status(attempt.getStatus())
                .startedAt(attempt.getStartedAt())
                .submittedAt(attempt.getSubmittedAt())
                .durationSeconds(attempt.getDurationSeconds())
                .score(attempt.getScore())
                .totalQuestions(attempt.getTotalQuestions() != null ? attempt.getTotalQuestions() : questions.size())
                .correctCount(attempt.getCorrectCount())
                .wrongCount(attempt.getWrongCount())
                .unansweredCount(attempt.getUnansweredCount())
                .answers(answerResponses)
                .build();
    }

    private MockExamAnswerResultResponse toAnswerResult(
            MockQuestion question,
            MockExamAnswer answer,
            boolean includeCorrect) {
        return MockExamAnswerResultResponse.builder()
                .questionId(question.getId())
                .orderIndex(question.getOrderIndex())
                .questionText(question.getQuestionText())
                .imageUrl(question.getImageUrl())
                .optionA(question.getOptionA())
                .optionB(question.getOptionB())
                .optionC(question.getOptionC())
                .optionD(question.getOptionD())
                .selectedOption(answer != null ? answer.getSelectedOption() : null)
                .correctOption(includeCorrect ? normalizeRequiredOption(question.getCorrectOption()) : null)
                .explanation(includeCorrect ? question.getExplanation() : null)
                .correct(answer != null ? Boolean.TRUE.equals(answer.getCorrect()) : false)
                .build();
    }

    private List<MockExamSubmitRequest.AnswerRequest> toSelectedAnswers(List<MockExamAnswer> answers) {
        return answers.stream()
                .filter(answer -> answer.getQuestion() != null && StringUtils.hasText(answer.getSelectedOption()))
                .map(answer -> new MockExamSubmitRequest.AnswerRequest(
                        answer.getQuestion().getId(),
                        normalizeOptionalUpper(answer.getSelectedOption())))
                .toList();
    }

    private Map<Long, String> normalizeAnswers(MockExamSubmitRequest request) {
        if (request == null || request.getAnswers() == null) {
            return Map.of();
        }

        Map<Long, String> selectedByQuestionId = new HashMap<>();
        for (MockExamSubmitRequest.AnswerRequest answer : request.getAnswers()) {
            if (answer == null || answer.getQuestionId() == null) {
                continue;
            }

            String selectedOption = normalizeOptionalUpper(answer.getSelectedOption());
            if (selectedOption != null && !ALLOWED_OPTIONS.contains(selectedOption)) {
                throw AppException.badRequest("Đáp án chỉ được là A, B, C hoặc D");
            }
            selectedByQuestionId.put(answer.getQuestionId(), selectedOption);
        }
        return selectedByQuestionId;
    }

    private void validateQuestionOwnership(List<MockQuestion> questions, Set<Long> submittedQuestionIds) {
        Set<Long> examQuestionIds = questions.stream()
                .map(MockQuestion::getId)
                .collect(Collectors.toCollection(HashSet::new));

        for (Long questionId : submittedQuestionIds) {
            if (!examQuestionIds.contains(questionId)) {
                throw AppException.badRequest("Câu hỏi không thuộc đề thi này");
            }
        }
    }

    private String normalizeOptionalUpper(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeRequiredOption(String value) {
        String normalized = normalizeOptionalUpper(value);
        if (normalized == null || !ALLOWED_OPTIONS.contains(normalized)) {
            throw AppException.badRequest("Đáp án đúng của câu hỏi không hợp lệ");
        }
        return normalized;
    }

    private Double calculateScore(int correctCount, int totalQuestions) {
        if (totalQuestions <= 0) {
            return 0.0;
        }

        return BigDecimal.valueOf(correctCount)
                .multiply(BigDecimal.TEN)
                .divide(BigDecimal.valueOf(totalQuestions), 2, RoundingMode.HALF_UP)
                .doubleValue();
    }

    private boolean isExpired(MockExamAttempt attempt, LocalDateTime now) {
        Integer durationMinutes = attempt.getExam().getDurationMinutes();
        if (durationMinutes == null || durationMinutes <= 0 || attempt.getStartedAt() == null) {
            return false;
        }
        return attempt.getStartedAt().plusMinutes(durationMinutes).isBefore(now);
    }

    private Long calculateDurationSeconds(LocalDateTime startedAt, LocalDateTime submittedAt) {
        if (startedAt == null || submittedAt == null) {
            return null;
        }
        return Math.max(Duration.between(startedAt, submittedAt).toSeconds(), 0);
    }

    private Long remainingSeconds(MockExamAttempt attempt) {
        Integer durationMinutes = attempt.getExam().getDurationMinutes();
        if (durationMinutes == null || durationMinutes <= 0 || attempt.getStartedAt() == null) {
            return null;
        }

        long seconds = Duration.between(LocalDateTime.now(), attempt.getStartedAt().plusMinutes(durationMinutes)).toSeconds();
        return Math.max(seconds, 0);
    }
}
