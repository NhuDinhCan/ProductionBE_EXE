package com.example.production.service;

import com.example.production.dto.RefreshTokenRequest;
import com.example.production.dto.RefreshTokenResponse;
import com.example.production.dto.SignInRequest;
import com.example.production.dto.SignInResponse;
import com.example.production.entity.InvalidtedToken;
import com.example.production.entity.User;
import com.example.production.exception.AppException;
import com.example.production.repositpry.InvalidtedTokenRepository;
import com.example.production.repositpry.UserRepository;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.text.ParseException;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final InvalidtedTokenRepository invalidtedTokenRepository;
    private final UserRepository userRepository;
    private final JwtDecoder jwtDecoder;

    public SignInResponse login(SignInRequest request) {
        var auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        User user = (User) auth.getPrincipal();
        return SignInResponse.builder()
                .accessToken(jwtService.generateAccessToken(user))
                .refreshToken(jwtService.generateRefreshToken(user))
                .build();
    }

    public void logout(String accessToken, String refreshToken) {
        revokeToken(accessToken);
        revokeToken(refreshToken);
        log.info("Logout completed");
    }

    public RefreshTokenResponse refreshToken(RefreshTokenRequest request) {
        if (!StringUtils.hasText(request.getRefreshToken())) {
            throw AppException.badRequest("Refresh token không được để trống");
        }

        Jwt jwt;
        try {
            jwt = jwtDecoder.decode(request.getRefreshToken());
        } catch (JwtException ex) {
            throw AppException.unauthorized("Refresh token không hợp lệ hoặc đã hết hạn");
        }

        if (!"refresh".equals(jwt.getClaimAsString("token_type"))) {
            throw AppException.unauthorized("Token không phải refresh token");
        }

        User user = userRepository.findByEmail(jwt.getSubject())
                .orElseThrow(() -> AppException.notFound("Người dùng không tồn tại"));

        revokeToken(request.getRefreshToken());

        return RefreshTokenResponse.builder()
                .accessToken(jwtService.generateAccessToken(user))
                .refreshToken(jwtService.generateRefreshToken(user))
                .build();
    }

    private void revokeToken(String token) {
        if (!StringUtils.hasText(token)) {
            return;
        }

        try {
            SignedJWT jwt = SignedJWT.parse(token);
            String jwtId = jwt.getJWTClaimsSet().getJWTID();
            if (!StringUtils.hasText(jwtId)) {
                throw AppException.unauthorized("Token không hợp lệ");
            }
            invalidtedTokenRepository.save(InvalidtedToken.builder()
                    .id(jwtId)
                    .expirationTime(jwt.getJWTClaimsSet().getExpirationTime())
                    .build());
        } catch (ParseException ex) {
            throw AppException.unauthorized("Token không hợp lệ");
        }
    }
}
