package com.example.production.service;


import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.example.production.entity.User;
import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import com.example.production.repositpry.InvalidtedTokenRepository;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JwtService {

    @Value("${jwt.secret-key}")
    private String secretKey;

    @Value("${jwt.access-token-expiration:1800000}")
    private long accessExpiration;

    @Value("${jwt.refresh-token-expiration:1209600000}")
    private long refreshExpiration;

    private final InvalidtedTokenRepository invalidtedTokenRepository;

    // HS384 nhất quán cho cả hai loại token
    private static final JWSAlgorithm ALGORITHM = JWSAlgorithm.HS384;

    public String generateAccessToken(User user) {
        return buildToken(user, accessExpiration, "access");
    }

    public String generateRefreshToken(User user) {
        return buildToken(user, refreshExpiration, "refresh");
    }

    private String buildToken(User user, long expirationMs, String tokenType) {
        JWTClaimsSet.Builder claimsBuilder = new JWTClaimsSet.Builder()
                .subject(user.getEmail())
                .issueTime(new Date())
                .expirationTime(new Date(Instant.now().toEpochMilli() + expirationMs))
                .jwtID(UUID.randomUUID().toString())
                .claim("userId", user.getId())
                .claim("first_name", user.getFirstName())
                .claim("token_type", tokenType);

        if ("access".equals(tokenType)) {
            List<String> roles = user.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority).toList();
            claimsBuilder.claim("authorities", roles);
        }

        JWTClaimsSet claims = claimsBuilder.build();

        JWSObject jws = new JWSObject(new JWSHeader(ALGORITHM), new Payload(claims.toJSONObject()));
        try {
            jws.sign(new MACSigner(secretKey.getBytes(StandardCharsets.UTF_8)));
            return jws.serialize();
        } catch (JOSEException e) {
            throw new RuntimeException("Không thể tạo JWT", e);
        }
    }

    public boolean verifyToken(String token) throws ParseException, JOSEException {
        if (StringUtils.isBlank(token)) return false;
        SignedJWT jwt = SignedJWT.parse(token);
        if (jwt.getJWTClaimsSet().getExpirationTime().before(new Date())) return false;
        if (invalidtedTokenRepository.existsById(jwt.getJWTClaimsSet().getJWTID())) return false;
        return jwt.verify(new MACVerifier(secretKey.getBytes(StandardCharsets.UTF_8)));
    }

    public String getEmailFromToken(String token) {
        try {
            return SignedJWT.parse(token).getJWTClaimsSet().getSubject();
        } catch (ParseException e) {
            throw new RuntimeException("Token không hợp lệ", e);
        }
    }

    public Long getUserIdFromToken(String token) {
        try {
            return SignedJWT.parse(token).getJWTClaimsSet().getLongClaim("userId");
        } catch (ParseException e) {
            throw new RuntimeException("Không thể lấy userId từ token", e);
        }
    }

    public List<String> getRolesFromToken(String token) {
        try {
            return SignedJWT.parse(token).getJWTClaimsSet().getStringListClaim("authorities");
        } catch (ParseException e) {
            return Collections.emptyList();
        }
    }
}
