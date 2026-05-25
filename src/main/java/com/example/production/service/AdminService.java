package com.example.production.service;

import com.example.production.entity.Career;
import com.example.production.entity.ExamCombination;
import com.example.production.entity.ExamCombinationSubject;
import com.example.production.entity.ExamSubject;
import com.example.production.entity.LearningMethod;
import com.example.production.entity.LearningResource;
import com.example.production.entity.LearningStrategyProfile;
import com.example.production.entity.MockExam;
import com.example.production.entity.MockExamAttempt;
import com.example.production.entity.MockQuestion;
import com.example.production.entity.Question;
import com.example.production.entity.QuestionCareerWeight;
import com.example.production.entity.Role;
import com.example.production.entity.University;
import com.example.production.entity.UniversityMajor;
import com.example.production.entity.User;
import com.example.production.entity.UserHashRole;
import com.example.production.exception.AppException;
import com.example.production.repositpry.CareerRepository;
import com.example.production.repositpry.ExamCombinationRepository;
import com.example.production.repositpry.ExamCombinationSubjectRepository;
import com.example.production.repositpry.ExamSubjectRepository;
import com.example.production.repositpry.LearningMethodRepository;
import com.example.production.repositpry.LearningResourceRepository;
import com.example.production.repositpry.LearningStrategyProfileRepository;
import com.example.production.repositpry.MockExamAttemptRepository;
import com.example.production.repositpry.MockExamRepository;
import com.example.production.repositpry.MockQuestionRepository;
import com.example.production.repositpry.QuestionCareerWeightRepository;
import com.example.production.repositpry.QuestionRepository;
import com.example.production.repositpry.RoleRepository;
import com.example.production.repositpry.UniversityMajorRepository;
import com.example.production.repositpry.UniversityRepository;
import com.example.production.repositpry.UserHashRoleRepository;
import com.example.production.repositpry.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AdminService {

    private static final Set<String> RESOURCE_TYPES = Set.of("PDF", "VIDEO", "WEBSITE", "EXERCISE", "ROADMAP");
    private static final Set<String> RESOURCE_LEVELS = Set.of("BEGINNER", "INTERMEDIATE", "ADVANCED");
    private static final Set<String> RESOURCE_STATUSES = Set.of("ACTIVE", "HIDDEN");
    private static final Set<String> DIFFICULTIES = Set.of("EASY", "MEDIUM", "HARD");
    private static final Set<String> MOCK_EXAM_STATUSES = Set.of("DRAFT", "ACTIVE", "HIDDEN");
    private static final Set<String> OPTIONS = Set.of("A", "B", "C", "D");
    private static final Set<String> ASSIGNABLE_ROLES = Set.of("USER", "ADMIN", "MENTOR");

    private final CareerRepository careerRepository;
    private final UniversityRepository universityRepository;
    private final UniversityMajorRepository universityMajorRepository;
    private final LearningMethodRepository learningMethodRepository;
    private final LearningStrategyProfileRepository learningStrategyProfileRepository;
    private final LearningResourceRepository learningResourceRepository;
    private final ExamSubjectRepository examSubjectRepository;
    private final ExamCombinationRepository examCombinationRepository;
    private final ExamCombinationSubjectRepository examCombinationSubjectRepository;
    private final MockExamRepository mockExamRepository;
    private final MockQuestionRepository mockQuestionRepository;
    private final MockExamAttemptRepository mockExamAttemptRepository;
    private final QuestionRepository questionRepository;
    private final QuestionCareerWeightRepository questionCareerWeightRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserHashRoleRepository userHashRoleRepository;

    public boolean isAdmin(Jwt jwt) {
        if (jwt == null || jwt.getClaimAsStringList("authorities") == null) {
            return false;
        }
        return jwt.getClaimAsStringList("authorities").contains("ADMIN");
    }

    @Transactional(readOnly = true)
    public Map<String, Object> dashboard() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalUsers", userRepository.count());
        result.put("totalCareers", careerRepository.count());
        result.put("totalUniversities", universityRepository.count());
        result.put("totalLearningResources", learningResourceRepository.count());
        result.put("totalMockExams", mockExamRepository.count());
        result.put("totalMockQuestions", mockQuestionRepository.count());
        result.put("totalAttempts", mockExamAttemptRepository.count());
        result.put("recentAttempts", mockExamAttemptRepository.findTop5ByOrderByStartedAtDesc().stream().map(this::attemptMap).toList());
        result.put("recentUsers", userRepository.findTop5ByOrderByIdDesc().stream().map(this::userMap).toList());
        return result;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> careers() {
        return careerRepository.findAll().stream().map(this::careerMap).toList();
    }

    @Transactional(readOnly = true)
    public Map<String, Object> career(Long id) {
        return careerMap(findCareer(id));
    }

    @Transactional
    public Map<String, Object> createCareer(Map<String, Object> request) {
        Career career = new Career(null, requiredString(request, "name"));
        return careerMap(careerRepository.save(career));
    }

    @Transactional
    public Map<String, Object> updateCareer(Long id, Map<String, Object> request) {
        Career career = findCareer(id);
        career.setName(requiredString(request, "name"));
        return careerMap(careerRepository.save(career));
    }

    @Transactional
    public void deleteCareer(Long id) {
        careerRepository.delete(findCareer(id));
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> universities() {
        return universityRepository.findAll().stream().map(this::universityMap).toList();
    }

    @Transactional(readOnly = true)
    public Map<String, Object> university(Long id) {
        return universityMap(findUniversity(id));
    }

    @Transactional
    public Map<String, Object> createUniversity(Map<String, Object> request) {
        University university = University.builder()
                .name(requiredString(request, "name"))
                .region(optionalString(request, "region"))
                .type(optionalString(request, "type"))
                .tuitionFee(optionalDouble(request, "tuitionFee"))
                .build();
        return universityMap(universityRepository.save(university));
    }

    @Transactional
    public Map<String, Object> updateUniversity(Long id, Map<String, Object> request) {
        University university = findUniversity(id);
        university.setName(requiredString(request, "name"));
        university.setRegion(optionalString(request, "region"));
        university.setType(optionalString(request, "type"));
        university.setTuitionFee(optionalDouble(request, "tuitionFee"));
        return universityMap(universityRepository.save(university));
    }

    @Transactional
    public void deleteUniversity(Long id) {
        universityRepository.delete(findUniversity(id));
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> universityMajors() {
        return universityMajorRepository.findAll().stream().map(this::universityMajorMap).toList();
    }

    @Transactional(readOnly = true)
    public Map<String, Object> universityMajor(Long id) {
        return universityMajorMap(findUniversityMajor(id));
    }

    @Transactional
    public Map<String, Object> createUniversityMajor(Map<String, Object> request) {
        UniversityMajor item = new UniversityMajor();
        applyUniversityMajor(item, request);
        return universityMajorMap(universityMajorRepository.save(item));
    }

    @Transactional
    public Map<String, Object> updateUniversityMajor(Long id, Map<String, Object> request) {
        UniversityMajor item = findUniversityMajor(id);
        applyUniversityMajor(item, request);
        return universityMajorMap(universityMajorRepository.save(item));
    }

    @Transactional
    public void deleteUniversityMajor(Long id) {
        universityMajorRepository.delete(findUniversityMajor(id));
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> learningMethods() {
        return learningMethodRepository.findAll().stream().map(this::learningMethodMap).toList();
    }

    @Transactional(readOnly = true)
    public Map<String, Object> learningMethod(Long id) {
        return learningMethodMap(findLearningMethod(id));
    }

    @Transactional
    public Map<String, Object> createLearningMethod(Map<String, Object> request) {
        LearningMethod method = new LearningMethod(
                null,
                requiredString(request, "title"),
                requiredString(request, "description"),
                optionalString(request, "benefits"));
        return learningMethodMap(learningMethodRepository.save(method));
    }

    @Transactional
    public Map<String, Object> updateLearningMethod(Long id, Map<String, Object> request) {
        LearningMethod method = findLearningMethod(id);
        method.setTitle(requiredString(request, "title"));
        method.setDescription(requiredString(request, "description"));
        method.setBenefits(optionalString(request, "benefits"));
        return learningMethodMap(learningMethodRepository.save(method));
    }

    @Transactional
    public void deleteLearningMethod(Long id) {
        learningMethodRepository.delete(findLearningMethod(id));
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> learningStrategyProfiles() {
        return learningStrategyProfileRepository.findAll().stream().map(this::learningStrategyProfileMap).toList();
    }

    @Transactional(readOnly = true)
    public Map<String, Object> learningStrategyProfile(Long id) {
        return learningStrategyProfileMap(findLearningStrategyProfile(id));
    }

    @Transactional
    public Map<String, Object> createLearningStrategyProfile(Map<String, Object> request) {
        LearningStrategyProfile profile = new LearningStrategyProfile();
        applyLearningStrategyProfile(profile, request);
        return learningStrategyProfileMap(learningStrategyProfileRepository.save(profile));
    }

    @Transactional
    public Map<String, Object> updateLearningStrategyProfile(Long id, Map<String, Object> request) {
        LearningStrategyProfile profile = findLearningStrategyProfile(id);
        applyLearningStrategyProfile(profile, request);
        return learningStrategyProfileMap(learningStrategyProfileRepository.save(profile));
    }

    @Transactional
    public void deleteLearningStrategyProfile(Long id) {
        learningStrategyProfileRepository.delete(findLearningStrategyProfile(id));
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> learningResources() {
        return learningResourceRepository.findAll().stream().map(this::learningResourceMap).toList();
    }

    @Transactional(readOnly = true)
    public Map<String, Object> learningResource(Long id) {
        return learningResourceMap(findLearningResource(id));
    }

    @Transactional
    public Map<String, Object> createLearningResource(Map<String, Object> request) {
        LearningResource resource = new LearningResource();
        applyLearningResource(resource, request);
        return learningResourceMap(learningResourceRepository.save(resource));
    }

    @Transactional
    public Map<String, Object> updateLearningResource(Long id, Map<String, Object> request) {
        LearningResource resource = findLearningResource(id);
        applyLearningResource(resource, request);
        return learningResourceMap(learningResourceRepository.save(resource));
    }

    @Transactional
    public void hideLearningResource(Long id) {
        LearningResource resource = findLearningResource(id);
        resource.setStatus("HIDDEN");
        learningResourceRepository.save(resource);
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> examSubjects() {
        return examSubjectRepository.findAllByOrderByNameAsc().stream().map(this::examSubjectMap).toList();
    }

    @Transactional(readOnly = true)
    public Map<String, Object> examSubject(Long id) {
        return examSubjectMap(findExamSubject(id));
    }

    @Transactional
    public Map<String, Object> createExamSubject(Map<String, Object> request) {
        ExamSubject subject = ExamSubject.builder()
                .code(optionalUpper(request, "code"))
                .name(requiredString(request, "name"))
                .build();
        return examSubjectMap(examSubjectRepository.save(subject));
    }

    @Transactional
    public Map<String, Object> updateExamSubject(Long id, Map<String, Object> request) {
        ExamSubject subject = findExamSubject(id);
        subject.setCode(optionalUpper(request, "code"));
        subject.setName(requiredString(request, "name"));
        return examSubjectMap(examSubjectRepository.save(subject));
    }

    @Transactional
    public void deleteExamSubject(Long id) {
        examSubjectRepository.delete(findExamSubject(id));
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> examCombinations() {
        return examCombinationRepository.findAllByOrderByCodeAsc().stream().map(this::examCombinationMap).toList();
    }

    @Transactional(readOnly = true)
    public Map<String, Object> examCombination(Long id) {
        return examCombinationMap(findExamCombination(id));
    }

    @Transactional
    public Map<String, Object> createExamCombination(Map<String, Object> request) {
        List<Long> subjectIds = requiredLongList(request, "subjectIds", "Vui lòng chọn ít nhất một môn thi");
        ExamCombination combination = ExamCombination.builder()
                .code(requiredString(request, "code").toUpperCase(Locale.ROOT))
                .name(requiredString(request, "name"))
                .description(optionalString(request, "description"))
                .build();
        combination = examCombinationRepository.save(combination);
        syncCombinationSubjects(combination, subjectIds);
        return examCombinationMap(combination);
    }

    @Transactional
    public Map<String, Object> updateExamCombination(Long id, Map<String, Object> request) {
        List<Long> subjectIds = requiredLongList(request, "subjectIds", "Vui lòng chọn ít nhất một môn thi");
        ExamCombination combination = findExamCombination(id);
        combination.setCode(requiredString(request, "code").toUpperCase(Locale.ROOT));
        combination.setName(requiredString(request, "name"));
        combination.setDescription(optionalString(request, "description"));
        combination = examCombinationRepository.save(combination);
        syncCombinationSubjects(combination, subjectIds);
        return examCombinationMap(combination);
    }

    @Transactional
    public void deleteExamCombination(Long id) {
        examCombinationSubjectRepository.deleteByCombinationId(id);
        examCombinationRepository.delete(findExamCombination(id));
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> mockExams() {
        return mockExamRepository.search(null, null, null, null, null, null).stream().map(this::mockExamMap).toList();
    }

    @Transactional(readOnly = true)
    public Map<String, Object> mockExam(Long id) {
        return mockExamMap(findMockExam(id));
    }

    @Transactional
    public Map<String, Object> createMockExam(Map<String, Object> request) {
        MockExam exam = new MockExam();
        applyMockExam(exam, request);
        return mockExamMap(mockExamRepository.save(exam));
    }

    @Transactional
    public Map<String, Object> updateMockExam(Long id, Map<String, Object> request) {
        MockExam exam = findMockExam(id);
        applyMockExam(exam, request);
        return mockExamMap(mockExamRepository.save(exam));
    }

    @Transactional
    public void deleteMockExam(Long id) {
        mockExamRepository.delete(findMockExam(id));
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> mockQuestions(Long examId) {
        return mockQuestionRepository.findByExamIdOrderByOrderIndexAscIdAsc(examId).stream().map(this::mockQuestionMap).toList();
    }

    @Transactional(readOnly = true)
    public Map<String, Object> mockQuestion(Long id) {
        return mockQuestionMap(findMockQuestion(id));
    }

    @Transactional
    public Map<String, Object> createMockQuestion(Long examId, Map<String, Object> request) {
        MockQuestion question = new MockQuestion();
        question.setExam(findMockExam(examId));
        applyMockQuestion(question, request, false);
        return mockQuestionMap(mockQuestionRepository.save(question));
    }

    @Transactional
    public Map<String, Object> updateMockQuestion(Long id, Map<String, Object> request) {
        MockQuestion question = findMockQuestion(id);
        applyMockQuestion(question, request, true);
        return mockQuestionMap(mockQuestionRepository.save(question));
    }

    @Transactional
    public void deleteMockQuestion(Long id) {
        mockQuestionRepository.delete(findMockQuestion(id));
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> quizQuestions() {
        return questionRepository.findAll().stream().map(this::questionMap).toList();
    }

    @Transactional(readOnly = true)
    public Map<String, Object> quizQuestion(Long id) {
        return questionMap(findQuestion(id));
    }

    @Transactional
    public Map<String, Object> createQuizQuestion(Map<String, Object> request) {
        Question question = new Question(null, requiredString(request, "content"));
        return questionMap(questionRepository.save(question));
    }

    @Transactional
    public Map<String, Object> updateQuizQuestion(Long id, Map<String, Object> request) {
        Question question = findQuestion(id);
        question.setContent(requiredString(request, "content"));
        return questionMap(questionRepository.save(question));
    }

    @Transactional
    public void deleteQuizQuestion(Long id) {
        questionCareerWeightRepository.findByQuestionId(id).forEach(questionCareerWeightRepository::delete);
        questionRepository.delete(findQuestion(id));
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> quizQuestionWeights(Long questionId) {
        return questionCareerWeightRepository.findByQuestionId(questionId).stream().map(this::weightMap).toList();
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> quizQuestionsWithWeights() {
        return questionRepository.findAll().stream()
                .map(question -> {
                    Map<String, Object> item = questionMap(question);
                    item.put("weights", questionCareerWeightRepository.findByQuestionId(question.getId()).stream().map(this::weightMap).toList());
                    return item;
                })
                .toList();
    }

    @Transactional
    public Map<String, Object> createQuizWeight(Long questionId, Map<String, Object> request) {
        Long careerId = requiredLong(request, "careerId");
        if (questionCareerWeightRepository.existsByQuestionIdAndCareerId(questionId, careerId)) {
            throw AppException.conflict("Ngành này đã có trọng số cho câu hỏi");
        }

        QuestionCareerWeight weight = new QuestionCareerWeight(
                null,
                findQuestion(questionId),
                findCareer(careerId),
                nonNegativeDouble(request, "weight"));
        return weightMap(questionCareerWeightRepository.save(weight));
    }

    @Transactional
    public Map<String, Object> updateQuizWeight(Long id, Map<String, Object> request) {
        QuestionCareerWeight weight = findWeight(id);
        Long careerId = requiredLong(request, "careerId");
        if (questionCareerWeightRepository.existsByQuestionIdAndCareerIdAndIdNot(weight.getQuestion().getId(), careerId, id)) {
            throw AppException.conflict("Ngành này đã có trọng số cho câu hỏi");
        }
        weight.setCareer(findCareer(careerId));
        weight.setWeight(nonNegativeDouble(request, "weight"));
        return weightMap(questionCareerWeightRepository.save(weight));
    }

    @Transactional
    public void deleteQuizWeight(Long id) {
        questionCareerWeightRepository.delete(findWeight(id));
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> users(String keyword) {
        List<User> users = StringUtils.hasText(keyword)
                ? userRepository.findByEmailContainingIgnoreCaseOrFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
                keyword.trim(), keyword.trim(), keyword.trim())
                : userRepository.findAll();
        return users.stream().map(this::userMap).toList();
    }

    @Transactional(readOnly = true)
    public Map<String, Object> user(Long id) {
        return userMap(findUser(id));
    }

    @Transactional
    public Map<String, Object> updateUser(Long id, Map<String, Object> request) {
        User user = findUser(id);
        user.setEmail(requiredString(request, "email"));
        user.setFirstName(requiredString(request, "firstName"));
        user.setLastName(requiredString(request, "lastName"));
        user.setPhone(optionalString(request, "phone"));
        user.setAddress(optionalString(request, "address"));
        user.setAvatar(optionalString(request, "avatar"));
        user.setBirthday(optionalDate(request, "birthday"));
        return userMap(userRepository.save(user));
    }

    @Transactional
    public Map<String, Object> updateUserRoles(Long id, Map<String, Object> request) {
        User user = findUser(id);
        List<String> roleNames = stringList(request, "roles").stream()
                .map(role -> role.trim().toUpperCase(Locale.ROOT))
                .filter(ASSIGNABLE_ROLES::contains)
                .distinct()
                .toList();
        if (roleNames.isEmpty()) {
            throw AppException.badRequest("User phải có ít nhất một role hợp lệ");
        }

        userHashRoleRepository.deleteByUserId(id);
        for (String roleName : roleNames) {
            Role role = roleRepository.findByName(roleName)
                    .orElseThrow(() -> AppException.notFound("Role không tồn tại: " + roleName));
            userHashRoleRepository.save(UserHashRole.builder().user(user).role(role).build());
        }
        Map<String, Object> response = userMap(user);
        response.put("roles", roleNames);
        return response;
    }

    @Transactional
    public void deleteUser(Long id) {
        userHashRoleRepository.deleteByUserId(id);
        userRepository.delete(findUser(id));
    }

    private void applyUniversityMajor(UniversityMajor item, Map<String, Object> request) {
        item.setUniversity(findUniversity(requiredLong(request, "universityId")));
        item.setCareer(findCareer(requiredLong(request, "careerId")));
        item.setScoreRequired(optionalDouble(request, "scoreRequired"));
    }

    private void applyLearningStrategyProfile(LearningStrategyProfile profile, Map<String, Object> request) {
        profile.setCareer(findCareer(requiredLong(request, "careerId")));
        profile.setDescription(requiredString(request, "description"));
        profile.setSkills(optionalString(request, "skills"));
        profile.setTools(optionalString(request, "tools"));
        profile.setWeeklyRoadmap(optionalString(request, "weeklyRoadmap"));
    }

    private void applyLearningResource(LearningResource resource, Map<String, Object> request) {
        resource.setCareer(findCareer(requiredLong(request, "careerId")));
        resource.setTitle(requiredString(request, "title"));
        resource.setDescription(optionalString(request, "description"));
        resource.setResourceType(requiredEnum(request, "resourceType", RESOURCE_TYPES));
        resource.setLevel(optionalEnum(request, "level", RESOURCE_LEVELS));
        resource.setUrl(optionalString(request, "url"));
        resource.setThumbnailUrl(optionalString(request, "thumbnailUrl"));
        resource.setStatus(optionalEnum(request, "status", RESOURCE_STATUSES, "ACTIVE"));
    }

    private void applyMockExam(MockExam exam, Map<String, Object> request) {
        exam.setSubject(findExamSubject(requiredLong(request, "subjectId")));
        Long combinationId = optionalLong(request, "combinationId");
        exam.setCombination(combinationId != null ? findExamCombination(combinationId) : null);
        exam.setTitle(requiredString(request, "title"));
        exam.setDifficulty(optionalEnum(request, "difficulty", DIFFICULTIES, "MEDIUM"));
        exam.setYear(optionalInteger(request, "year"));
        exam.setDurationMinutes(optionalInteger(request, "durationMinutes"));
        String status = optionalEnum(request, "status", MOCK_EXAM_STATUSES);
        if (StringUtils.hasText(status)) {
            exam.setStatus(status);
        } else if (!StringUtils.hasText(exam.getStatus())) {
            exam.setStatus("DRAFT");
        }
    }

    private void applyMockQuestion(MockQuestion question, Map<String, Object> request, boolean update) {
        Integer orderIndex = requiredInteger(request, "orderIndex");
        if (update
                ? mockQuestionRepository.existsByExamIdAndOrderIndexAndIdNot(question.getExam().getId(), orderIndex, question.getId())
                : mockQuestionRepository.existsByExamIdAndOrderIndex(question.getExam().getId(), orderIndex)) {
            throw AppException.conflict("orderIndex đã tồn tại trong đề này");
        }

        question.setOrderIndex(orderIndex);
        question.setQuestionText(requiredString(request, "questionText"));
        String imageUrl = optionalString(request, "imageUrl");
        question.setImageUrl(StringUtils.hasText(imageUrl) ? imageUrl : null);
        question.setOptionA(requiredString(request, "optionA"));
        question.setOptionB(requiredString(request, "optionB"));
        question.setOptionC(requiredString(request, "optionC"));
        question.setOptionD(requiredString(request, "optionD"));
        question.setCorrectOption(requiredEnum(request, "correctOption", OPTIONS));
        question.setExplanation(optionalString(request, "explanation"));
    }

    private void syncCombinationSubjects(ExamCombination combination, List<Long> subjectIds) {
        examCombinationSubjectRepository.deleteByCombinationId(combination.getId());
        for (Long subjectId : subjectIds.stream().distinct().toList()) {
            examCombinationSubjectRepository.save(ExamCombinationSubject.builder()
                    .combination(combination)
                    .subject(findExamSubject(subjectId))
                    .build());
        }
    }

    private Map<String, Object> careerMap(Career career) {
        return mapOf("id", career.getId(), "name", career.getName());
    }

    private Map<String, Object> universityMap(University university) {
        return mapOf("id", university.getId(), "name", university.getName(), "region", university.getRegion(),
                "type", university.getType(), "tuitionFee", university.getTuitionFee());
    }

    private Map<String, Object> universityMajorMap(UniversityMajor item) {
        return mapOf("id", item.getId(), "universityId", item.getUniversity() != null ? item.getUniversity().getId() : null,
                "universityName", item.getUniversity() != null ? item.getUniversity().getName() : null,
                "careerId", item.getCareer() != null ? item.getCareer().getId() : null,
                "careerName", item.getCareer() != null ? item.getCareer().getName() : null,
                "scoreRequired", item.getScoreRequired());
    }

    private Map<String, Object> learningMethodMap(LearningMethod method) {
        return mapOf("id", method.getId(), "title", method.getTitle(), "description", method.getDescription(), "benefits", method.getBenefits());
    }

    private Map<String, Object> learningStrategyProfileMap(LearningStrategyProfile profile) {
        return mapOf("id", profile.getId(), "careerId", profile.getCareer().getId(), "careerName", profile.getCareer().getName(),
                "description", profile.getDescription(), "skills", profile.getSkills(), "tools", profile.getTools(),
                "weeklyRoadmap", profile.getWeeklyRoadmap());
    }

    private Map<String, Object> learningResourceMap(LearningResource resource) {
        return mapOf("id", resource.getId(), "careerId", resource.getCareer().getId(), "careerName", resource.getCareer().getName(),
                "title", resource.getTitle(), "description", resource.getDescription(), "resourceType", resource.getResourceType(),
                "level", resource.getLevel(), "url", resource.getUrl(), "thumbnailUrl", resource.getThumbnailUrl(), "status", resource.getStatus());
    }

    private Map<String, Object> examSubjectMap(ExamSubject subject) {
        return mapOf("id", subject.getId(), "code", subject.getCode(), "name", subject.getName());
    }

    private Map<String, Object> examCombinationMap(ExamCombination combination) {
        List<ExamCombinationSubject> subjects = examCombinationSubjectRepository.findByCombinationIdWithSubject(combination.getId());
        return mapOf("id", combination.getId(), "code", combination.getCode(), "name", combination.getName(),
                "description", combination.getDescription(),
                "subjectIds", subjects.stream().map(item -> item.getSubject().getId()).toList(),
                "subjects", subjects.stream().map(item -> examSubjectMap(item.getSubject())).toList());
    }

    private Map<String, Object> mockExamMap(MockExam exam) {
        return mapOf("id", exam.getId(), "subjectId", exam.getSubject().getId(), "subjectName", exam.getSubject().getName(),
                "combinationId", exam.getCombination() != null ? exam.getCombination().getId() : null,
                "combinationCode", exam.getCombination() != null ? exam.getCombination().getCode() : null,
                "title", exam.getTitle(), "difficulty", exam.getDifficulty(), "year", exam.getYear(),
                "durationMinutes", exam.getDurationMinutes(), "status", exam.getStatus(),
                "totalQuestions", mockQuestionRepository.countByExamId(exam.getId()));
    }

    private Map<String, Object> mockQuestionMap(MockQuestion question) {
        return mapOf("id", question.getId(), "examId", question.getExam().getId(), "examTitle", question.getExam().getTitle(),
                "orderIndex", question.getOrderIndex(), "questionText", question.getQuestionText(),
                "imageUrl", question.getImageUrl(),
                "optionA", question.getOptionA(), "optionB", question.getOptionB(), "optionC", question.getOptionC(),
                "optionD", question.getOptionD(), "correctOption", question.getCorrectOption(), "explanation", question.getExplanation());
    }

    private Map<String, Object> questionMap(Question question) {
        return mapOf("id", question.getId(), "content", question.getContent());
    }

    private Map<String, Object> weightMap(QuestionCareerWeight weight) {
        return mapOf("id", weight.getId(), "questionId", weight.getQuestion().getId(), "questionContent", weight.getQuestion().getContent(),
                "careerId", weight.getCareer().getId(), "careerName", weight.getCareer().getName(), "weight", weight.getWeight());
    }

    private Map<String, Object> userMap(User user) {
        return mapOf("id", user.getId(), "email", user.getEmail(), "firstName", user.getFirstName(), "lastName", user.getLastName(),
                "phone", user.getPhone(), "address", user.getAddress(), "birthday", user.getBirthday(), "avatar", user.getAvatar(),
                "roles", user.getUserHasRoles() == null ? List.of() : user.getUserHasRoles().stream()
                        .filter(item -> item.getRole() != null)
                        .map(item -> item.getRole().getName())
                        .distinct()
                        .toList());
    }

    private Map<String, Object> attemptMap(MockExamAttempt attempt) {
        return mapOf("id", attempt.getId(), "examId", attempt.getExam().getId(), "examTitle", attempt.getExam().getTitle(),
                "userId", attempt.getUser().getId(), "userEmail", attempt.getUser().getEmail(), "status", attempt.getStatus(),
                "score", attempt.getScore(), "startedAt", attempt.getStartedAt(), "submittedAt", attempt.getSubmittedAt());
    }

    private Career findCareer(Long id) {
        return careerRepository.findById(id).orElseThrow(() -> AppException.notFound("Không tìm thấy ngành học"));
    }

    private University findUniversity(Long id) {
        return universityRepository.findById(id).orElseThrow(() -> AppException.notFound("Không tìm thấy trường đại học"));
    }

    private UniversityMajor findUniversityMajor(Long id) {
        return universityMajorRepository.findById(id).orElseThrow(() -> AppException.notFound("Không tìm thấy điểm chuẩn"));
    }

    private LearningMethod findLearningMethod(Long id) {
        return learningMethodRepository.findById(id).orElseThrow(() -> AppException.notFound("Không tìm thấy phương pháp học"));
    }

    private LearningStrategyProfile findLearningStrategyProfile(Long id) {
        return learningStrategyProfileRepository.findById(id).orElseThrow(() -> AppException.notFound("Không tìm thấy hồ sơ chiến lược"));
    }

    private LearningResource findLearningResource(Long id) {
        return learningResourceRepository.findById(id).orElseThrow(() -> AppException.notFound("Không tìm thấy tài liệu"));
    }

    private ExamSubject findExamSubject(Long id) {
        return examSubjectRepository.findById(id).orElseThrow(() -> AppException.notFound("Không tìm thấy môn thi"));
    }

    private ExamCombination findExamCombination(Long id) {
        return examCombinationRepository.findById(id).orElseThrow(() -> AppException.notFound("Không tìm thấy tổ hợp"));
    }

    private MockExam findMockExam(Long id) {
        return mockExamRepository.findById(id).orElseThrow(() -> AppException.notFound("Không tìm thấy đề thi"));
    }

    private MockQuestion findMockQuestion(Long id) {
        return mockQuestionRepository.findById(id).orElseThrow(() -> AppException.notFound("Không tìm thấy câu hỏi"));
    }

    private Question findQuestion(Long id) {
        return questionRepository.findById(id).orElseThrow(() -> AppException.notFound("Không tìm thấy câu hỏi quiz"));
    }

    private QuestionCareerWeight findWeight(Long id) {
        return questionCareerWeightRepository.findById(id).orElseThrow(() -> AppException.notFound("Không tìm thấy trọng số"));
    }

    private User findUser(Long id) {
        return userRepository.findById(id).orElseThrow(() -> AppException.notFound("Không tìm thấy user"));
    }

    private String requiredString(Map<String, Object> request, String key) {
        String value = optionalString(request, key);
        if (!StringUtils.hasText(value)) {
            throw AppException.badRequest(key + " không được để trống");
        }
        return value;
    }

    private String optionalString(Map<String, Object> request, String key) {
        Object value = request.get(key);
        return value == null ? null : value.toString().trim();
    }

    private Long requiredLong(Map<String, Object> request, String key) {
        Long value = optionalLong(request, key);
        if (value == null) {
            throw AppException.badRequest(key + " không được để trống");
        }
        return value;
    }

    private Long optionalLong(Map<String, Object> request, String key) {
        Object value = request.get(key);
        if (value == null || !StringUtils.hasText(value.toString())) return null;
        return Long.valueOf(value.toString());
    }

    private Integer requiredInteger(Map<String, Object> request, String key) {
        Integer value = optionalInteger(request, key);
        if (value == null) throw AppException.badRequest(key + " không được để trống");
        return value;
    }

    private Integer optionalInteger(Map<String, Object> request, String key) {
        Object value = request.get(key);
        if (value == null || !StringUtils.hasText(value.toString())) return null;
        return Integer.valueOf(value.toString());
    }

    private Double optionalDouble(Map<String, Object> request, String key) {
        Object value = request.get(key);
        if (value == null || !StringUtils.hasText(value.toString())) return null;
        return Double.valueOf(value.toString());
    }

    private Double nonNegativeDouble(Map<String, Object> request, String key) {
        Double value = optionalDouble(request, key);
        if (value == null || value < 0) {
            throw AppException.badRequest(key + " phải là số >= 0");
        }
        return value;
    }

    private LocalDate optionalDate(Map<String, Object> request, String key) {
        String value = optionalString(request, key);
        return StringUtils.hasText(value) ? LocalDate.parse(value) : null;
    }

    private String optionalUpper(Map<String, Object> request, String key) {
        String value = optionalString(request, key);
        return StringUtils.hasText(value) ? value.toUpperCase(Locale.ROOT) : null;
    }

    private String requiredEnum(Map<String, Object> request, String key, Set<String> allowed) {
        String value = optionalEnum(request, key, allowed);
        if (!StringUtils.hasText(value)) throw AppException.badRequest(key + " không hợp lệ");
        return value;
    }

    private String optionalEnum(Map<String, Object> request, String key, Set<String> allowed) {
        return optionalEnum(request, key, allowed, null);
    }

    private String optionalEnum(Map<String, Object> request, String key, Set<String> allowed, String defaultValue) {
        String value = optionalString(request, key);
        if (!StringUtils.hasText(value)) return defaultValue;
        String normalized = value.toUpperCase(Locale.ROOT);
        if (!allowed.contains(normalized)) throw AppException.badRequest(key + " không hợp lệ");
        return normalized;
    }

    @SuppressWarnings("unchecked")
    private List<Long> longList(Map<String, Object> request, String key) {
        Object value = request.get(key);
        if (!(value instanceof Collection<?> values)) return List.of();
        List<Long> result = new ArrayList<>();
        for (Object item : values) {
            if (item != null && StringUtils.hasText(item.toString())) result.add(Long.valueOf(item.toString()));
        }
        return result;
    }

    private List<Long> requiredLongList(Map<String, Object> request, String key, String message) {
        List<Long> values = longList(request, key).stream().distinct().toList();
        if (values.isEmpty()) {
            throw AppException.badRequest(message);
        }
        return values;
    }

    private List<String> stringList(Map<String, Object> request, String key) {
        Object value = request.get(key);
        if (!(value instanceof Collection<?> values)) return List.of();
        List<String> result = new ArrayList<>();
        for (Object item : values) {
            if (item != null && StringUtils.hasText(item.toString())) result.add(item.toString());
        }
        return result;
    }

    private Map<String, Object> mapOf(Object... values) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (int i = 0; i < values.length; i += 2) {
            map.put((String) values[i], values[i + 1]);
        }
        return map;
    }
}
