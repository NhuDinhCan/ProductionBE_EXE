package com.example.production.controller;

import com.example.production.dto.ApiResponse;
import com.example.production.dto.ApplyLearningMethodRequest;
import com.example.production.dto.StudyScheduleRequest;
import com.example.production.dto.StudyScheduleResponse;
import com.example.production.service.StudyScheduleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/study-schedules", produces = "application/json;charset=UTF-8")
@RequiredArgsConstructor
public class StudyScheduleController {

    private final StudyScheduleService studyScheduleService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<StudyScheduleResponse>>> getSchedules(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) Long careerId,
            @RequestParam(required = false) Long learningMethodId
    ) {
        return ResponseEntity.ok(ApiResponse.<List<StudyScheduleResponse>>builder()
                .code(200)
                .message("Lấy thời khóa biểu thành công")
                .result(studyScheduleService.getSchedules(jwt, careerId, learningMethodId))
                .build());
    }


    @GetMapping("/today")
    public ResponseEntity<ApiResponse<List<StudyScheduleResponse>>> getTodaySchedules(
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(ApiResponse.<List<StudyScheduleResponse>>builder()
                .code(200)
                .message("Lấy lịch học hôm nay thành công")
                .result(studyScheduleService.getTodaySchedules(jwt))
                .build());
    }

    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<List<StudyScheduleResponse>>> generateSchedules(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody ApplyLearningMethodRequest request) {
        return ResponseEntity.ok(ApiResponse.<List<StudyScheduleResponse>>builder()
                .code(200)
                .message("Tạo lịch học từ phương pháp đã áp dụng thành công")
                .result(studyScheduleService.generateSchedules(jwt, request))
                .build());
    }

    @PostMapping
    public ResponseEntity<ApiResponse<StudyScheduleResponse>> createSchedule(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody StudyScheduleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.<StudyScheduleResponse>builder()
                .code(201)
                .message("Tạo lịch học thành công")
                .result(studyScheduleService.createSchedule(jwt, request))
                .build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<StudyScheduleResponse>> updateSchedule(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id,
            @Valid @RequestBody StudyScheduleRequest request) {
        return ResponseEntity.ok(ApiResponse.<StudyScheduleResponse>builder()
                .code(200)
                .message("Cập nhật lịch học thành công")
                .result(studyScheduleService.updateSchedule(jwt, id, request))
                .build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteSchedule(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id) {
        studyScheduleService.deleteSchedule(jwt, id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .code(200)
                .message("Xóa lịch học thành công")
                .build());
    }
}
