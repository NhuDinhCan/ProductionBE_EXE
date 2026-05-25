package com.example.production.controller;

import com.example.production.dto.AiChatRequest;
import com.example.production.dto.AiChatResponse;
import com.example.production.dto.ApiResponse;
import com.example.production.service.AiChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ai")
public class AiChatController {

    private final AiChatService aiChatService;

    @PostMapping("/chat")
    public ResponseEntity<ApiResponse<AiChatResponse>> chat(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody AiChatRequest request) {
        return ResponseEntity.ok(ApiResponse.<AiChatResponse>builder()
                .code(200)
                .message("OK")
                .result(aiChatService.chat(jwt, request))
                .build());
    }
}
