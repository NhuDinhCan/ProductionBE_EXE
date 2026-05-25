package com.example.production.controller;

import com.example.production.dto.ApiResponse;
import com.example.production.dto.LearningResourceResponse;
import com.example.production.service.LearningResourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "/api/learning-resources", produces = "application/json;charset=UTF-8")
@RequiredArgsConstructor
public class LearningResourceController {

    private final LearningResourceService learningResourceService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<LearningResourceResponse>>> getResources(
            @RequestParam(required = false) Long careerId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String keyword,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(ApiResponse.<List<LearningResourceResponse>>builder()
                .code(200)
                .message("Lấy danh sách tài liệu thành công")
                .result(learningResourceService.getResources(careerId, type, keyword, jwt))
                .build());
    }

    @GetMapping("/saved")
    public ResponseEntity<ApiResponse<List<LearningResourceResponse>>> getSavedResources(
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(ApiResponse.<List<LearningResourceResponse>>builder()
                .code(200)
                .message("Lấy tài liệu đã lưu thành công")
                .result(learningResourceService.getSavedResources(jwt))
                .build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<LearningResourceResponse>> getResource(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(ApiResponse.<LearningResourceResponse>builder()
                .code(200)
                .message("Lấy tài liệu thành công")
                .result(learningResourceService.getResource(id, jwt))
                .build());
    }

    @PostMapping("/{id}/save")
    public ResponseEntity<ApiResponse<LearningResourceResponse>> saveResource(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.<LearningResourceResponse>builder()
                .code(200)
                .message("Đã lưu tài liệu")
                .result(learningResourceService.saveResource(jwt, id))
                .build());
    }

    @DeleteMapping("/{id}/save")
    public ResponseEntity<ApiResponse<Void>> unsaveResource(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id) {
        learningResourceService.unsaveResource(jwt, id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .code(200)
                .message("Đã bỏ lưu tài liệu")
                .build());
    }
}
