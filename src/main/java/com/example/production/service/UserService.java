package com.example.production.service;


import com.example.production.dto.UserCreationRequest;
import com.example.production.dto.UserCreationResponse;
import com.example.production.entity.Role;
import com.example.production.entity.User;
import com.example.production.entity.UserHashRole;
import com.example.production.exception.AppException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.example.production.repositpry.RoleRepository;
import com.example.production.repositpry.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    /** Đăng ký công khai → role USER */
    public UserCreationResponse createUser(UserCreationRequest request) {
        return createWithRole(request, "USER");
    }

    /** Chỉ ADMIN gọi được (kiểm soát ở controller) → role MENTOR */
    public UserCreationResponse createMentor(UserCreationRequest request) {
        return createWithRole(request, "MENTOR");
    }

    private UserCreationResponse createWithRole(UserCreationRequest request, String roleName) {
        if (userRepository.existsByEmail(request.getEmail()))
            throw AppException.conflict("Email đã được sử dụng");

        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> AppException.notFound("Role " + roleName + " không tồn tại"));

        User user = User.builder()
                .email(request.getEmail())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .birthday(request.getBirthday())
                .address(request.getAddress())
                .build();

        user.setUserHasRoles(List.of(
                UserHashRole.builder().user(user).role(role).build()
        ));

        userRepository.save(user);

        return UserCreationResponse.builder()
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .build();
    }
}
