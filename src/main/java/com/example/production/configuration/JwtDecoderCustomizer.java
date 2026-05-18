package com.example.production.configuration;

import com.example.production.repositpry.InvalidtedTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class JwtDecoderCustomizer implements JwtDecoder {

    private final InvalidtedTokenRepository invalidtedTokenRepository;

    @Value("${jwt.secret-key}")
    private String secretKey;

    private volatile NimbusJwtDecoder nimbusJwtDecoder;

    @Override
    public Jwt decode(String token) throws JwtException {
        Jwt jwt = decoder().decode(token);
        String jwtId = jwt.getId();

        if (!StringUtils.hasText(jwtId)) {
            throw new JwtException("JWT ID is required");
        }
        if (invalidtedTokenRepository.existsById(jwtId)) {
            throw new JwtException("Token has been revoked");
        }

        return jwt;
    }

    private NimbusJwtDecoder decoder() {
        NimbusJwtDecoder localDecoder = nimbusJwtDecoder;
        if (localDecoder == null) {
            synchronized (this) {
                localDecoder = nimbusJwtDecoder;
                if (localDecoder == null) {
                    if (!StringUtils.hasText(secretKey)) {
                        throw new JwtException("JWT secret key is not configured");
                    }
                    SecretKeySpec secretKeySpec = new SecretKeySpec(
                            secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA384");
                    localDecoder = NimbusJwtDecoder
                            .withSecretKey(secretKeySpec)
                            .macAlgorithm(MacAlgorithm.HS384)
                            .build();
                    nimbusJwtDecoder = localDecoder;
                }
            }
        }
        return localDecoder;
    }
}
