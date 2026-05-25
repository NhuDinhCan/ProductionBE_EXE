package com.example.production.controller;


import com.example.production.dto.ApiResponse;
import com.example.production.dto.UserCreationRequest;
import com.example.production.dto.UserCreationResponse;
import com.example.production.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import com.example.production.repositpry.UserRepository;
import com.example.production.service.UserService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;

    /** Đăng ký tài khoản USER - public */
    @PostMapping
    public ResponseEntity<ApiResponse<UserCreationResponse>> register(
            @Valid @RequestBody UserCreationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.<UserCreationResponse>builder()
                        .code(201).message("Đăng ký thành công")
                        .result(userService.createUser(request))
                        .build());
    }

    /** Lấy danh sách user - phải đăng nhập */
    @GetMapping
    public ResponseEntity<?> getUsers(
            @RequestParam(defaultValue = "false") boolean excludeSelf,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal Jwt jwt) {
        String email = jwt.getSubject();
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 50);
        Pageable pageable = PageRequest.of(safePage, safeSize);
        Page<User> users = excludeSelf
                ? userRepository.findByEmailNot(email, pageable)
                : userRepository.findAll(pageable);

        var result = users.getContent().stream().map(u -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", u.getId());
            m.put("email", u.getEmail());
            m.put("firstName", u.getFirstName() != null ? u.getFirstName() : "");
            m.put("lastName", u.getLastName() != null ? u.getLastName() : "");
            m.put("avatar", u.getAvatar() != null ? u.getAvatar() : "");
            return m;
        }).toList();

        return ResponseEntity.ok(Map.of(
                "content", result,
                "page", users.getNumber(),
                "size", users.getSize(),
                "totalElements", users.getTotalElements(),
                "totalPages", users.getTotalPages(),
                "hasNext", users.hasNext()
        ));
    }

    /** Chỉ ADMIN tạo được MENTOR */
    @PostMapping("/mentors")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ApiResponse<UserCreationResponse>> createMentor(
            @Valid @RequestBody UserCreationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.<UserCreationResponse>builder()
                        .code(201).message("Tạo mentor thành công")
                        .result(userService.createMentor(request))
                        .build());
    }
}
