package com.example.production.controller;

import com.example.production.dto.ApiResponse;
import com.example.production.dto.LogoutRequest;
import com.example.production.dto.RefreshTokenRequest;
import com.example.production.dto.RefreshTokenResponse;
import com.example.production.dto.SignInRequest;
import com.example.production.dto.SignInResponse;
import com.example.production.service.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
            @RequestHeader("Authorization") String authHeader,
            @RequestBody(required = false) LogoutRequest request) {
        String refreshToken = request != null ? request.getRefreshToken() : null;
        authenticationService.logout(authHeader.replace("Bearer ", ""), refreshToken);
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
