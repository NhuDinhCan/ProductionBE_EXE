package com.example.production.configuration;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 20)
public class RateLimitFilter extends OncePerRequestFilter {

    private static final Set<String> AUTH_ENDPOINTS = Set.of(
            "/api/auth/login",
            "/api/auth/refresh"
    );

    private final Map<String, Window> windows = new ConcurrentHashMap<>();
    private final AtomicInteger requestCounter = new AtomicInteger();

    @Value("${app.rate-limit.enabled:true}")
    private boolean enabled;

    @Value("${app.rate-limit.window-seconds:60}")
    private long windowSeconds;

    @Value("${app.rate-limit.auth-limit:20}")
    private int authLimit;

    @Value("${app.rate-limit.register-limit:10}")
    private int registerLimit;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        if (!enabled || !isLimitedEndpoint(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        long now = System.currentTimeMillis();
        long windowMillis = windowSeconds * 1000;
        String key = clientIp(request) + ":" + request.getMethod() + ":" + request.getRequestURI();
        int limit = limitFor(request);
        AtomicBoolean rejected = new AtomicBoolean(false);

        windows.compute(key, (ignored, current) -> {
            if (current == null || now - current.startedAt >= windowMillis) {
                return new Window(now, 1);
            }
            if (current.count >= limit) {
                rejected.set(true);
                return current;
            }
            return new Window(current.startedAt, current.count + 1);
        });

        if (requestCounter.incrementAndGet() % 1000 == 0) {
            windows.entrySet().removeIf(entry -> now - entry.getValue().startedAt >= windowMillis * 2);
        }

        if (rejected.get()) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setHeader("Retry-After", String.valueOf(windowSeconds));
            response.setContentType("application/json");
            response.getWriter().write("{\"code\":429,\"message\":\"Too many requests\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isLimitedEndpoint(HttpServletRequest request) {
        if (!HttpMethod.POST.matches(request.getMethod())) {
            return false;
        }
        String path = request.getRequestURI();
        return AUTH_ENDPOINTS.contains(path) || "/api/users".equals(path);
    }

    private int limitFor(HttpServletRequest request) {
        return "/api/users".equals(request.getRequestURI()) ? registerLimit : authLimit;
    }

    private String clientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private record Window(long startedAt, int count) {
    }
}
