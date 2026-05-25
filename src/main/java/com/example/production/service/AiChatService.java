package com.example.production.service;

import com.example.production.dto.AiChatRequest;
import com.example.production.dto.AiChatResponse;
import com.example.production.exception.AppException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiChatService {

    private static final String SYSTEM_PROMPT = """
            Bạn là EduBot 4.0 của TGrowth Pro, trợ lý học tập cá nhân cho học sinh/sinh viên Việt Nam.
            Trả lời ngắn gọn, dễ hiểu, có gợi ý hành động cụ thể.
            Nếu không có dữ liệu trong hệ thống thì nói rõ là chưa có dữ liệu, không bịa.
            """;

    private static final String FALLBACK_ANSWER = "EduBot hiện chưa phản hồi được, bạn thử lại sau nhé.";

    private final AiAssistantContextService contextService;
    private final ObjectMapper objectMapper;
    private final Map<String, Window> rateLimitWindows = new ConcurrentHashMap<>();
    private final AtomicInteger rateLimitRequestCounter = new AtomicInteger();

    @Value("${ai.api-key:}")
    private String apiKey;

    @Value("${ai.model:qwen/qwen3.7-max}")
    private String model;

    @Value("${ai.base-url:https://openrouter.ai/api/v1/chat/completions}")
    private String baseUrl;

    @Value("${ai.timeout-seconds:60}")
    private int timeoutSeconds;

    @Value("${ai.rate-limit-per-minute:20}")
    private int rateLimitPerMinute;

    @Value("${ai.max-tokens:1024}")
    private int maxTokens;

    @Value("${app.allowed-origins:http://localhost:5173}")
    private String allowedOrigins;

    private RestClient restClient;

    @PostConstruct
    void init() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(10));
        requestFactory.setReadTimeout(Duration.ofSeconds(timeoutSeconds));

        this.restClient = RestClient.builder()
                .requestFactory(requestFactory)
                .build();
    }

    public AiChatResponse chat(Jwt jwt, AiChatRequest request) {
        if (jwt == null || !StringUtils.hasText(jwt.getSubject())) {
            throw AppException.unauthorized("Vui lòng đăng nhập");
        }

        if (request == null || !StringUtils.hasText(request.getMessage())) {
            throw AppException.badRequest("Nội dung câu hỏi không được để trống");
        }

        enforceRateLimit(jwt.getSubject());

        String message = request.getMessage().trim();
        String contextType = normalizeContextType(request.getContextType());

        String safeContext;
        try {
            safeContext = contextService.buildContext(jwt, message, contextType);
        } catch (AppException ex) {
            throw ex;
        } catch (Exception ex) {
            log.warn("AI context collection failed: {}", ex.getClass().getSimpleName());
            safeContext = "Chưa lấy được context từ database TGrowth Pro do lỗi hệ thống tạm thời. Không được bịa dữ liệu cá nhân.";
        }

        if (!StringUtils.hasText(apiKey)) {
            log.warn("AI provider is not configured");
            return fallbackResponse();
        }

        try {
            JsonNode response = restClient.post()
                    .uri(baseUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + apiKey)
                    .header("HTTP-Referer", firstAllowedOrigin())
                    .header("X-Title", "TGrowth Pro")
                    .body(openRouterPayload(message, contextType, safeContext))
                    .retrieve()
                    .body(JsonNode.class);

            String content = response == null
                    ? ""
                    : response.path("choices").path(0).path("message").path("content").asText("");

            return parseAiContent(content, contextType);

        } catch (RestClientResponseException ex) {
            log.warn("AI provider request failed: status={}, responseBody={}",
                    ex.getStatusCode().value(),
                    safeProviderResponseBody(ex.getResponseBodyAsString()));
            return fallbackResponse();
        } catch (Exception ex) {
            log.warn("AI provider request failed: {}", ex.getClass().getSimpleName());
            return fallbackResponse();
        }
    }

    private Map<String, Object> openRouterPayload(String message, String contextType, String safeContext) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("model", model);
        payload.put("temperature", 0.3);
        payload.put("max_tokens", maxTokens);
        payload.put("messages", List.of(
                Map.of("role", "system", "content", SYSTEM_PROMPT),
                Map.of("role", "user", "content", userPrompt(message, contextType, safeContext))
        ));
        return payload;
    }

    private String userPrompt(String message, String contextType, String safeContext) {
        return """
                Câu hỏi của user:
                %s

                Loại ngữ cảnh FE gửi lên: %s

                CONTEXT AN TOÀN TỪ DATABASE TGrowth Pro:
                %s

                Quy tắc trả lời:
                - Chỉ dùng dữ liệu trong CONTEXT khi nói về dữ liệu cá nhân, lịch học, tài liệu đã lưu, kết quả luyện đề hoặc ngành đang quan tâm.
                - Nếu CONTEXT nói chưa có dữ liệu thì nói rõ chưa có dữ liệu, không tự bịa.
                - Không nhắc tới token, JWT, API key, password hoặc thông tin nhạy cảm.
                - Trả lời bằng tiếng Việt, ngắn gọn, dễ hiểu, có bước tiếp theo cụ thể.
                - Trả về JSON hợp lệ đúng schema:
                  {"answer":"nội dung trả lời","suggestions":["gợi ý 1","gợi ý 2","gợi ý 3"]}
                """.formatted(message, contextType, safeContext);
    }

    private AiChatResponse parseAiContent(String content, String contextType) {
        if (!StringUtils.hasText(content)) {
            return fallbackResponse();
        }

        String candidate = extractJson(content);
        try {
            JsonNode root = objectMapper.readTree(candidate);
            String answer = root.path("answer").asText("");
            List<String> suggestions = new ArrayList<>();

            JsonNode suggestionNodes = root.path("suggestions");
            if (suggestionNodes.isArray()) {
                suggestionNodes.forEach(node -> {
                    String value = node.asText("").trim();
                    if (StringUtils.hasText(value) && suggestions.size() < 3) {
                        suggestions.add(value);
                    }
                });
            }

            if (!StringUtils.hasText(answer)) {
                return fallbackResponse();
            }

            return AiChatResponse.builder()
                    .answer(answer.trim())
                    .suggestions(suggestions.isEmpty() ? defaultSuggestions(contextType) : suggestions)
                    .build();

        } catch (Exception ignored) {
            return AiChatResponse.builder()
                    .answer(content.trim())
                    .suggestions(defaultSuggestions(contextType))
                    .build();
        }
    }

    private String extractJson(String content) {
        String value = content.trim();

        if (value.startsWith("```")) {
            int firstLineEnd = value.indexOf('\n');
            int lastFence = value.lastIndexOf("```");
            if (firstLineEnd >= 0 && lastFence > firstLineEnd) {
                value = value.substring(firstLineEnd + 1, lastFence).trim();
            }
        }

        int start = value.indexOf('{');
        int end = value.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return value.substring(start, end + 1);
        }

        return value;
    }

    private String safeProviderResponseBody(String responseBody) {
        if (!StringUtils.hasText(responseBody)) {
            return "<empty>";
        }

        String sanitized = responseBody;
        if (StringUtils.hasText(apiKey)) {
            sanitized = sanitized.replace(apiKey, "[REDACTED]");
        }

        int maxLogLength = 4000;
        if (sanitized.length() > maxLogLength) {
            return sanitized.substring(0, maxLogLength) + "...[truncated]";
        }

        return sanitized;
    }

    private void enforceRateLimit(String subject) {
        long now = System.currentTimeMillis();
        long windowMillis = 60_000;
        AtomicBoolean rejected = new AtomicBoolean(false);

        rateLimitWindows.compute(subject, (key, current) -> {
            if (current == null || now - current.startedAt >= windowMillis) {
                return new Window(now, 1);
            }

            if (current.count >= rateLimitPerMinute) {
                rejected.set(true);
                return current;
            }

            return new Window(current.startedAt, current.count + 1);
        });

        if (rateLimitRequestCounter.incrementAndGet() % 1000 == 0) {
            rateLimitWindows.entrySet().removeIf(entry -> now - entry.getValue().startedAt >= windowMillis * 2);
        }

        if (rejected.get()) {
            throw AppException.tooManyRequests("Bạn đang hỏi quá nhanh, vui lòng thử lại sau ít phút.");
        }
    }

    private AiChatResponse fallbackResponse() {
        return AiChatResponse.builder()
                .answer(FALLBACK_ANSWER)
                .suggestions(defaultSuggestions("GENERAL"))
                .build();
    }

    private List<String> defaultSuggestions(String contextType) {
        return switch (normalizeContextType(contextType)) {
            case "SCHEDULE" -> List.of("Gợi ý lịch học tuần này", "Tạo lịch học", "Xem lịch hôm nay");
            case "RESOURCE" -> List.of("Gợi ý tài liệu", "Xem tài liệu đã lưu", "Tài liệu theo ngành");
            case "MOCK_EXAM" -> List.of("Phân tích kết quả luyện đề", "Cách ôn câu sai", "Luyện đề tiếp");
            case "STUDY_METHOD" -> List.of("Tôi nên học phương pháp nào?", "Áp dụng phương pháp học", "Xem lộ trình học");
            case "CAREER" -> List.of("Ngành nào phù hợp với tôi?", "Xem chiến lược học tập", "Gợi ý trường phù hợp");
            default -> List.of("Gợi ý lịch học", "Xem tài liệu", "Luyện đề");
        };
    }

    private String normalizeContextType(String contextType) {
        if (!StringUtils.hasText(contextType)) {
            return "GENERAL";
        }
        return contextType.trim().toUpperCase(Locale.ROOT);
    }

    private String firstAllowedOrigin() {
        if (!StringUtils.hasText(allowedOrigins)) {
            return "http://localhost:5173";
        }

        return allowedOrigins.split(",")[0].trim();
    }

    private record Window(long startedAt, int count) {
    }
}