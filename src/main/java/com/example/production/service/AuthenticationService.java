package com.example.production.service;


import com.example.production.dto.RefreshTokenRequest;
import com.example.production.dto.RefreshTokenResponse;
import com.example.production.dto.SignInRequest;
import com.example.production.dto.SignInResponse;
import com.example.production.entity.InvalidtedToken;
import com.example.production.entity.User;
import com.example.production.exception.AppException;
import com.nimbusds.jwt.SignedJWT;
import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Service;
import com.example.production.repositpry.InvalidtedTokenRepository;
import com.example.production.repositpry.UserRepository;

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

    public void logout(String accessToken) throws ParseException {
        SignedJWT jwt = SignedJWT.parse(accessToken);
        invalidtedTokenRepository.save(InvalidtedToken.builder()
                .id(jwt.getJWTClaimsSet().getJWTID())
                .token(accessToken)
                .expirationTime(jwt.getJWTClaimsSet().getExpirationTime())
                .build());
        log.info("Logout: {}", jwt.getJWTClaimsSet().getSubject());
    }

    public RefreshTokenResponse refreshToken(RefreshTokenRequest request) {
        if (StringUtils.isBlank(request.getRefreshToken()))
            throw AppException.badRequest("Refresh token không được để trống");

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

        return RefreshTokenResponse.builder()
                .accessToken(jwtService.generateAccessToken(user))
                .build();
    }
}
