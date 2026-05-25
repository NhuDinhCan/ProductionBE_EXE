package com.example.production.controller;

import com.example.production.dto.ApiResponse;
import com.example.production.dto.AdminImportResult;
import com.example.production.service.MockExamImportService;
import com.example.production.service.MockQuestionImageService;
import com.example.production.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

@RestController
@RequestMapping(value = "/api/admin", produces = "application/json;charset=UTF-8")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ADMIN')")
public class AdminController {

    private final AdminService adminService;
    private final MockExamImportService mockExamImportService;
    private final MockQuestionImageService mockQuestionImageService;

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<Map<String, Object>>> dashboard() {
        return ok(adminService.dashboard());
    }

    @GetMapping("/careers")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> careers() { return ok(adminService.careers()); }
    @GetMapping("/careers/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> career(@PathVariable Long id) { return ok(adminService.career(id)); }
    @PostMapping("/careers")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createCareer(@RequestBody Map<String, Object> request) { return ok(adminService.createCareer(request)); }
    @PutMapping("/careers/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateCareer(@PathVariable Long id, @RequestBody Map<String, Object> request) { return ok(adminService.updateCareer(id, request)); }
    @DeleteMapping("/careers/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCareer(@PathVariable Long id) { return deleted(() -> adminService.deleteCareer(id)); }

    @GetMapping("/universities")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> universities() { return ok(adminService.universities()); }
    @GetMapping("/universities/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> university(@PathVariable Long id) { return ok(adminService.university(id)); }
    @PostMapping("/universities")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createUniversity(@RequestBody Map<String, Object> request) { return ok(adminService.createUniversity(request)); }
    @PutMapping("/universities/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateUniversity(@PathVariable Long id, @RequestBody Map<String, Object> request) { return ok(adminService.updateUniversity(id, request)); }
    @DeleteMapping("/universities/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUniversity(@PathVariable Long id) { return deleted(() -> adminService.deleteUniversity(id)); }

    @GetMapping("/university-majors")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> universityMajors() { return ok(adminService.universityMajors()); }
    @GetMapping("/university-majors/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> universityMajor(@PathVariable Long id) { return ok(adminService.universityMajor(id)); }
    @PostMapping("/university-majors")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createUniversityMajor(@RequestBody Map<String, Object> request) { return ok(adminService.createUniversityMajor(request)); }
    @PutMapping("/university-majors/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateUniversityMajor(@PathVariable Long id, @RequestBody Map<String, Object> request) { return ok(adminService.updateUniversityMajor(id, request)); }
    @DeleteMapping("/university-majors/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUniversityMajor(@PathVariable Long id) { return deleted(() -> adminService.deleteUniversityMajor(id)); }

    @GetMapping("/learning-methods")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> learningMethods() { return ok(adminService.learningMethods()); }
    @GetMapping("/learning-methods/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> learningMethod(@PathVariable Long id) { return ok(adminService.learningMethod(id)); }
    @PostMapping("/learning-methods")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createLearningMethod(@RequestBody Map<String, Object> request) { return ok(adminService.createLearningMethod(request)); }
    @PutMapping("/learning-methods/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateLearningMethod(@PathVariable Long id, @RequestBody Map<String, Object> request) { return ok(adminService.updateLearningMethod(id, request)); }
    @DeleteMapping("/learning-methods/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteLearningMethod(@PathVariable Long id) { return deleted(() -> adminService.deleteLearningMethod(id)); }

    @GetMapping("/learning-strategy-profiles")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> learningStrategyProfiles() { return ok(adminService.learningStrategyProfiles()); }
    @GetMapping("/learning-strategy-profiles/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> learningStrategyProfile(@PathVariable Long id) { return ok(adminService.learningStrategyProfile(id)); }
    @PostMapping("/learning-strategy-profiles")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createLearningStrategyProfile(@RequestBody Map<String, Object> request) { return ok(adminService.createLearningStrategyProfile(request)); }
    @PutMapping("/learning-strategy-profiles/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateLearningStrategyProfile(@PathVariable Long id, @RequestBody Map<String, Object> request) { return ok(adminService.updateLearningStrategyProfile(id, request)); }
    @DeleteMapping("/learning-strategy-profiles/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteLearningStrategyProfile(@PathVariable Long id) { return deleted(() -> adminService.deleteLearningStrategyProfile(id)); }

    @GetMapping("/learning-resources")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> learningResources() { return ok(adminService.learningResources()); }
    @GetMapping("/learning-resources/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> learningResource(@PathVariable Long id) { return ok(adminService.learningResource(id)); }
    @PostMapping("/learning-resources")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createLearningResource(@RequestBody Map<String, Object> request) { return ok(adminService.createLearningResource(request)); }
    @PutMapping("/learning-resources/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateLearningResource(@PathVariable Long id, @RequestBody Map<String, Object> request) { return ok(adminService.updateLearningResource(id, request)); }
    @DeleteMapping("/learning-resources/{id}")
    public ResponseEntity<ApiResponse<Void>> hideLearningResource(@PathVariable Long id) { return deleted(() -> adminService.hideLearningResource(id)); }

    @GetMapping("/exam-subjects")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> examSubjects() { return ok(adminService.examSubjects()); }
    @GetMapping("/exam-subjects/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> examSubject(@PathVariable Long id) { return ok(adminService.examSubject(id)); }
    @PostMapping("/exam-subjects")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createExamSubject(@RequestBody Map<String, Object> request) { return ok(adminService.createExamSubject(request)); }
    @PutMapping("/exam-subjects/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateExamSubject(@PathVariable Long id, @RequestBody Map<String, Object> request) { return ok(adminService.updateExamSubject(id, request)); }
    @DeleteMapping("/exam-subjects/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteExamSubject(@PathVariable Long id) { return deleted(() -> adminService.deleteExamSubject(id)); }

    @GetMapping("/exam-combinations")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> examCombinations() { return ok(adminService.examCombinations()); }
    @GetMapping("/exam-combinations/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> examCombination(@PathVariable Long id) { return ok(adminService.examCombination(id)); }
    @PostMapping("/exam-combinations")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createExamCombination(@RequestBody Map<String, Object> request) { return ok(adminService.createExamCombination(request)); }
    @PutMapping("/exam-combinations/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateExamCombination(@PathVariable Long id, @RequestBody Map<String, Object> request) { return ok(adminService.updateExamCombination(id, request)); }
    @DeleteMapping("/exam-combinations/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteExamCombination(@PathVariable Long id) { return deleted(() -> adminService.deleteExamCombination(id)); }

    @GetMapping("/mock-exams")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> mockExams() { return ok(adminService.mockExams()); }
    @GetMapping("/mock-exams/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> mockExam(@PathVariable Long id) { return ok(adminService.mockExam(id)); }
    @PostMapping("/mock-exams")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createMockExam(@RequestBody Map<String, Object> request) { return ok(adminService.createMockExam(request)); }
    @PutMapping("/mock-exams/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateMockExam(@PathVariable Long id, @RequestBody Map<String, Object> request) { return ok(adminService.updateMockExam(id, request)); }
    @DeleteMapping("/mock-exams/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteMockExam(@PathVariable Long id) { return deleted(() -> adminService.deleteMockExam(id)); }

    @PostMapping(value = "/mock-exams/{examId}/upload-questions", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<AdminImportResult>> uploadMockExamQuestions(
            @PathVariable Long examId,
            @RequestParam("file") MultipartFile file) {
        return importResult(mockExamImportService.uploadQuestions(examId, file));
    }

    @PostMapping(value = "/mock-exams/{examId}/upload-answers", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<AdminImportResult>> uploadMockExamAnswers(
            @PathVariable Long examId,
            @RequestParam("file") MultipartFile file) {
        return importResult(mockExamImportService.uploadAnswers(examId, file));
    }

    @GetMapping("/mock-exams/{examId}/questions/preview")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> previewMockExamQuestions(@PathVariable Long examId) {
        return ok(mockExamImportService.previewQuestions(examId));
    }

    @PutMapping("/mock-exams/{examId}/publish")
    public ResponseEntity<ApiResponse<Map<String, Object>>> publishMockExam(@PathVariable Long examId) {
        return ok(mockExamImportService.publish(examId));
    }

    @GetMapping("/mock-exams/{examId}/questions")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> mockQuestions(@PathVariable Long examId) { return ok(adminService.mockQuestions(examId)); }
    @GetMapping("/mock-questions/{questionId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> mockQuestion(@PathVariable Long questionId) { return ok(adminService.mockQuestion(questionId)); }
    @PostMapping("/mock-exams/{examId}/questions")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createMockQuestion(@PathVariable Long examId, @RequestBody Map<String, Object> request) { return ok(adminService.createMockQuestion(examId, request)); }
    @PutMapping("/mock-questions/{questionId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateMockQuestion(@PathVariable Long questionId, @RequestBody Map<String, Object> request) { return ok(adminService.updateMockQuestion(questionId, request)); }
    @DeleteMapping("/mock-questions/{questionId}")
    public ResponseEntity<ApiResponse<Void>> deleteMockQuestion(@PathVariable Long questionId) { return deleted(() -> adminService.deleteMockQuestion(questionId)); }
    @PostMapping(value = "/mock-questions/upload-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Map<String, Object>>> uploadMockQuestionImage(@RequestParam("file") MultipartFile file) { return ok(mockQuestionImageService.upload(file)); }

    @GetMapping("/quiz/questions")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> quizQuestions() { return ok(adminService.quizQuestions()); }
    @GetMapping("/quiz/questions-with-weights")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> quizQuestionsWithWeights() { return ok(adminService.quizQuestionsWithWeights()); }
    @GetMapping("/quiz/questions/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> quizQuestion(@PathVariable Long id) { return ok(adminService.quizQuestion(id)); }
    @PostMapping("/quiz/questions")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createQuizQuestion(@RequestBody Map<String, Object> request) { return ok(adminService.createQuizQuestion(request)); }
    @PutMapping("/quiz/questions/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateQuizQuestion(@PathVariable Long id, @RequestBody Map<String, Object> request) { return ok(adminService.updateQuizQuestion(id, request)); }
    @DeleteMapping("/quiz/questions/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteQuizQuestion(@PathVariable Long id) { return deleted(() -> adminService.deleteQuizQuestion(id)); }

    @GetMapping("/quiz/questions/{questionId}/weights")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> quizQuestionWeights(@PathVariable Long questionId) { return ok(adminService.quizQuestionWeights(questionId)); }
    @PostMapping("/quiz/questions/{questionId}/weights")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createQuizWeight(@PathVariable Long questionId, @RequestBody Map<String, Object> request) { return ok(adminService.createQuizWeight(questionId, request)); }
    @PutMapping("/quiz/weights/{weightId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateQuizWeight(@PathVariable Long weightId, @RequestBody Map<String, Object> request) { return ok(adminService.updateQuizWeight(weightId, request)); }
    @DeleteMapping("/quiz/weights/{weightId}")
    public ResponseEntity<ApiResponse<Void>> deleteQuizWeight(@PathVariable Long weightId) { return deleted(() -> adminService.deleteQuizWeight(weightId)); }

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> users(@RequestParam(required = false) String keyword) { return ok(adminService.users(keyword)); }
    @GetMapping("/users/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> user(@PathVariable Long id) { return ok(adminService.user(id)); }
    @PutMapping("/users/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateUser(@PathVariable Long id, @RequestBody Map<String, Object> request) { return ok(adminService.updateUser(id, request)); }
    @PutMapping("/users/{id}/roles")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateUserRoles(@PathVariable Long id, @RequestBody Map<String, Object> request) { return ok(adminService.updateUserRoles(id, request)); }
    @DeleteMapping("/users/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) { return deleted(() -> adminService.deleteUser(id)); }

    private <T> ResponseEntity<ApiResponse<T>> ok(T result) {
        return ResponseEntity.ok(ApiResponse.<T>builder().code(200).message("OK").result(result).build());
    }

    private ResponseEntity<ApiResponse<AdminImportResult>> importResult(AdminImportResult result) {
        boolean success = result != null && result.isSuccess();
        return ResponseEntity.status(success ? HttpStatus.OK : HttpStatus.BAD_REQUEST)
                .body(ApiResponse.<AdminImportResult>builder()
                        .code(success ? 200 : 400)
                        .message(success ? "OK" : "File không hợp lệ")
                        .result(result)
                        .build());
    }

    private ResponseEntity<ApiResponse<Void>> deleted(Runnable action) {
        action.run();
        return ResponseEntity.ok(ApiResponse.<Void>builder().code(200).message("Đã xóa").build());
    }
}
