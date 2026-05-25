package com.example.production.controller;

import com.example.production.dto.ApiResponse;
import com.example.production.dto.MockExamAttemptDetailResponse;
import com.example.production.dto.MockExamAttemptSummaryResponse;
import com.example.production.dto.MockExamCombinationResponse;
import com.example.production.dto.MockExamResponse;
import com.example.production.dto.MockExamStartResponse;
import com.example.production.dto.MockExamSubjectResponse;
import com.example.production.dto.MockExamSubmitRequest;
import com.example.production.service.MockExamService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "/api/mock-exams", produces = "application/json;charset=UTF-8")
@RequiredArgsConstructor
public class MockExamController {

    private final MockExamService mockExamService;

    @GetMapping("/subjects")
    public ResponseEntity<ApiResponse<List<MockExamSubjectResponse>>> getSubjects() {
        return ResponseEntity.ok(ApiResponse.<List<MockExamSubjectResponse>>builder()
                .code(200)
                .message("Lấy danh sách môn học thành công")
                .result(mockExamService.getSubjects())
                .build());
    }

    @GetMapping("/combinations")
    public ResponseEntity<ApiResponse<List<MockExamCombinationResponse>>> getCombinations() {
        return ResponseEntity.ok(ApiResponse.<List<MockExamCombinationResponse>>builder()
                .code(200)
                .message("Lấy danh sách tổ hợp thành công")
                .result(mockExamService.getCombinations())
                .build());
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<MockExamResponse>>> getExams(
            @RequestParam(required = false) Long subjectId,
            @RequestParam(required = false) String combinationCode,
            @RequestParam(required = false) String difficulty,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(ApiResponse.<List<MockExamResponse>>builder()
                .code(200)
                .message("Lấy danh sách đề thi thành công")
                .result(mockExamService.getExams(subjectId, combinationCode, difficulty, year, keyword))
                .build());
    }

    @GetMapping("/{examId}")
    public ResponseEntity<ApiResponse<MockExamResponse>> getExam(@PathVariable Long examId) {
        return ResponseEntity.ok(ApiResponse.<MockExamResponse>builder()
                .code(200)
                .message("Lấy đề thi thành công")
                .result(mockExamService.getExam(examId))
                .build());
    }

    @PostMapping(value = "/{examId}/start", produces = "application/json;charset=UTF-8")
    public ResponseEntity<ApiResponse<MockExamStartResponse>> startExam(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long examId) {
        return ResponseEntity.ok(ApiResponse.<MockExamStartResponse>builder()
                .code(200)
                .message("Bắt đầu làm bài thành công")
                .result(mockExamService.startExam(jwt, examId))
                .build());
    }

    @PostMapping("/attempts/{attemptId}/submit")
    public ResponseEntity<ApiResponse<MockExamAttemptDetailResponse>> submitAttempt(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long attemptId,
            @RequestBody(required = false) MockExamSubmitRequest request) {
        return ResponseEntity.ok(ApiResponse.<MockExamAttemptDetailResponse>builder()
                .code(200)
                .message("Nộp bài thành công")
                .result(mockExamService.submitAttempt(jwt, attemptId, request))
                .build());
    }

    @GetMapping("/attempts")
    public ResponseEntity<ApiResponse<List<MockExamAttemptSummaryResponse>>> getAttempts(
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(ApiResponse.<List<MockExamAttemptSummaryResponse>>builder()
                .code(200)
                .message("Lấy lịch sử làm bài thành công")
                .result(mockExamService.getAttempts(jwt))
                .build());
    }

    @GetMapping("/attempts/{attemptId}/resume")
    public ResponseEntity<ApiResponse<MockExamStartResponse>> resumeAttempt(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long attemptId) {
        return ResponseEntity.ok(ApiResponse.<MockExamStartResponse>builder()
                .code(200)
                .message("Tiáº¿p tá»¥c lÃ m bÃ i thÃ nh cÃ´ng")
                .result(mockExamService.resumeAttempt(jwt, attemptId))
                .build());
    }

    @GetMapping("/attempts/{attemptId}")
    public ResponseEntity<ApiResponse<MockExamAttemptDetailResponse>> getAttempt(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long attemptId) {
        return ResponseEntity.ok(ApiResponse.<MockExamAttemptDetailResponse>builder()
                .code(200)
                .message("Lấy kết quả bài làm thành công")
                .result(mockExamService.getAttempt(jwt, attemptId))
                .build());
    }

    @DeleteMapping("/attempts/{attemptId}")
    public ResponseEntity<ApiResponse<Void>> deleteAttempt(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long attemptId) {
        mockExamService.deleteAttempt(jwt, attemptId);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .code(200)
                .message("XÃ³a lá»‹ch sá»­ bÃ i lÃ m thÃ nh cÃ´ng")
                .build());
    }

    @DeleteMapping("/attempts")
    public ResponseEntity<ApiResponse<Void>> deleteAllAttempts(
            @AuthenticationPrincipal Jwt jwt) {
        mockExamService.deleteAllAttempts(jwt);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .code(200)
                .message("XÃ³a toÃ n bá»™ lá»‹ch sá»­ lÃ m bÃ i thÃ nh cÃ´ng")
                .build());
    }
}
