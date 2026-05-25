package com.example.production.configuration;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@Component
@Profile("prod")
@RequiredArgsConstructor
public class ProductionEnvironmentValidator {

    private final Environment environment;

    @PostConstruct
    void validate() {
        validateDatabaseUrl();
        validateJwtSecret();
        validateAllowedOrigins();
        validateBrokerCredentials();
        validateAiConfig();
    }

    private void validateDatabaseUrl() {
        String dbUrl = required("DB_URL");
        rejectPlaceholder("DB_URL", dbUrl);
        String normalized = dbUrl.toLowerCase(Locale.ROOT);
        if (!normalized.startsWith("jdbc:mysql:")) {
            fail("DB_URL must use a MySQL JDBC URL in production");
        }
        if (normalized.contains("usessl=false") || normalized.contains("sslmode=disabled")) {
            fail("DB_URL must not disable MySQL TLS in production");
        }
        boolean tlsConfigured = normalized.contains("usessl=true")
                || normalized.contains("sslmode=required")
                || normalized.contains("sslmode=verify_ca")
                || normalized.contains("sslmode=verify_identity");
        if (!tlsConfigured) {
            fail("DB_URL must enable MySQL TLS in production");
        }
        rejectLocalValue("DB_URL", normalized);
    }

    private void validateJwtSecret() {
        String secret = required("JWT_SECRET");
        rejectPlaceholder("JWT_SECRET", secret);
        if (secret.length() < 64) {
            fail("JWT_SECRET must be at least 64 characters in production");
        }
    }

    private void validateAllowedOrigins() {
        String origins = required("ALLOWED_ORIGINS");
        rejectPlaceholder("ALLOWED_ORIGINS", origins);
        List<String> values = Arrays.stream(origins.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .toList();
        if (values.isEmpty()) {
            fail("ALLOWED_ORIGINS must contain at least one HTTPS frontend origin");
        }
        for (String origin : values) {
            String normalized = origin.toLowerCase(Locale.ROOT);
            if ("*".equals(origin) || origin.contains("*")) {
                fail("ALLOWED_ORIGINS must not contain wildcard origins in production");
            }
            if (!normalized.startsWith("https://")) {
                fail("ALLOWED_ORIGINS must use HTTPS origins in production: " + origin);
            }
            rejectLocalValue("ALLOWED_ORIGINS", normalized);
        }
    }

    private void validateBrokerCredentials() {
        boolean relayEnabled = environment.getProperty(
                "WEBSOCKET_BROKER_RELAY_ENABLED", Boolean.class, true);
        if (!relayEnabled) {
            fail("WEBSOCKET_BROKER_RELAY_ENABLED must be true in production");
        }

        for (String key : List.of(
                "WEBSOCKET_BROKER_RELAY_HOST",
                "WEBSOCKET_BROKER_RELAY_CLIENT_LOGIN",
                "WEBSOCKET_BROKER_RELAY_CLIENT_PASSCODE",
                "WEBSOCKET_BROKER_RELAY_SYSTEM_LOGIN",
                "WEBSOCKET_BROKER_RELAY_SYSTEM_PASSCODE")) {
            String value = required(key);
            rejectPlaceholder(key, value);
            if ("guest".equalsIgnoreCase(value)) {
                fail(key + " must not use RabbitMQ guest credentials in production");
            }
        }
    }

    private void validateAiConfig() {
        String apiKey = required("AI_API_KEY");
        rejectPlaceholder("AI_API_KEY", apiKey);
        String baseUrl = required("AI_BASE_URL");
        rejectPlaceholder("AI_BASE_URL", baseUrl);
        if (!baseUrl.toLowerCase(Locale.ROOT).startsWith("https://")) {
            fail("AI_BASE_URL must use HTTPS in production");
        }
        String model = required("AI_MODEL");
        rejectPlaceholder("AI_MODEL", model);
    }

    private String required(String key) {
        String value = environment.getProperty(key);
        if (!StringUtils.hasText(value)) {
            fail(key + " is required in production");
        }
        return value.trim();
    }

    private void rejectPlaceholder(String key, String value) {
        String normalized = value.toLowerCase(Locale.ROOT);
        if (normalized.contains("replace_with")
                || normalized.contains("example.com")
                || normalized.contains("<")
                || normalized.contains(">")) {
            fail(key + " still contains a placeholder value");
        }
    }

    private void rejectLocalValue(String key, String normalizedValue) {
        if (normalizedValue.contains("localhost")
                || normalizedValue.contains("127.0.0.1")
                || normalizedValue.contains("host.docker.internal")) {
            fail(key + " must not point to a local development host in production");
        }
    }

    private void fail(String message) {
        throw new IllegalStateException("Invalid production configuration: " + message);
    }
}
