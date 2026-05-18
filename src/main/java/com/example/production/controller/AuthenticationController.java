package com.example.production.controller;


import com.example.production.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.production.service.AuthenticationService;

import java.text.ParseException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<SignInResponse>> login(@Valid @RequestBody SignInRequest request) {
        return ResponseEntity.ok(ApiResponse.<SignInResponse>builder()
                .code(200).message("Đăng nhập thành công")
                .result(authenticationService.login(request))
                .build());
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestHeader("Authorization") String authHeader) throws ParseException {
        authenticationService.logout(authHeader.replace("Bearer ", ""));
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .code(200).message("Đăng xuất thành công").build());
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<RefreshTokenResponse>> refresh(
            @Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(ApiResponse.<RefreshTokenResponse>builder()
                .code(200).message("Làm mới token thành công")
                .result(authenticationService.refreshToken(request))
                .build());
    }
}
