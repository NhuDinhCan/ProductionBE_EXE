package com.example.production.controller;

import com.example.production.dto.ApiResponse;
import com.example.production.dto.ApplyLearningMethodRequest;
import com.example.production.dto.UserLearningMethodResponse;
import com.example.production.service.UserLearningMethodService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "/api/user-learning-methods", produces = "application/json;charset=UTF-8")
@RequiredArgsConstructor
public class UserLearningMethodController {

    private final UserLearningMethodService userLearningMethodService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<UserLearningMethodResponse>>> getAppliedMethods(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) Long careerId) {
        return ResponseEntity.ok(ApiResponse.<List<UserLearningMethodResponse>>builder()
                .code(200)
                .message("Lấy danh sách phương pháp đã áp dụng thành công")
                .result(userLearningMethodService.getAppliedMethods(jwt, careerId))
                .build());
    }

    @PostMapping("/apply")
    public ResponseEntity<ApiResponse<UserLearningMethodResponse>> applyMethod(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody ApplyLearningMethodRequest request) {
        return ResponseEntity.ok(ApiResponse.<UserLearningMethodResponse>builder()
                .code(200)
                .message("Áp dụng phương pháp học thành công")
                .result(userLearningMethodService.applyMethod(jwt, request))
                .build());
    }
}

