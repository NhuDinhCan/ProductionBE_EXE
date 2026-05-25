package com.example.production.service;

import com.example.production.dto.LearningResourceResponse;
import com.example.production.entity.LearningResource;
import com.example.production.entity.User;
import com.example.production.entity.UserSavedResource;
import com.example.production.exception.AppException;
import com.example.production.repositpry.LearningResourceRepository;
import com.example.production.repositpry.UserRepository;
import com.example.production.repositpry.UserSavedResourceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LearningResourceService {

    private static final String ACTIVE = "ACTIVE";
    private static final Set<String> ALLOWED_TYPES = Set.of("PDF", "VIDEO", "WEBSITE", "EXERCISE", "ROADMAP");

    private final LearningResourceRepository learningResourceRepository;
    private final UserSavedResourceRepository userSavedResourceRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<LearningResourceResponse> getResources(Long careerId, String type, String keyword, Jwt jwt) {
        String normalizedType = normalizeType(type);
        String normalizedKeyword = StringUtils.hasText(keyword) ? keyword.trim() : null;
        List<LearningResource> resources = learningResourceRepository.search(ACTIVE, careerId, normalizedType, normalizedKeyword);
        Map<Long, UserSavedResource> savedByResourceId = savedByResourceId(jwt);

        return resources.stream()
                .map(resource -> toResponse(resource, savedByResourceId.get(resource.getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public LearningResourceResponse getResource(Long id, Jwt jwt) {
        LearningResource resource = getActiveResource(id);
        Map<Long, UserSavedResource> savedByResourceId = savedByResourceId(jwt);
        return toResponse(resource, savedByResourceId.get(resource.getId()));
    }

    @Transactional(readOnly = true)
    public List<LearningResourceResponse> getSavedResources(Jwt jwt) {
        User user = currentUser(jwt);
        return userSavedResourceRepository.findByUserIdOrderBySavedAtDesc(user.getId()).stream()
                .filter(saved -> ACTIVE.equals(saved.getResource().getStatus()))
                .map(saved -> toResponse(saved.getResource(), saved))
                .toList();
    }

    @Transactional
    public LearningResourceResponse saveResource(Jwt jwt, Long resourceId) {
        User user = currentUser(jwt);
        LearningResource resource = getActiveResource(resourceId);
        UserSavedResource saved = userSavedResourceRepository
                .findByUserIdAndResourceId(user.getId(), resource.getId())
                .orElseGet(() -> userSavedResourceRepository.save(UserSavedResource.builder()
                        .user(user)
                        .resource(resource)
                        .build()));

        return toResponse(resource, saved);
    }

    @Transactional
    public void unsaveResource(Jwt jwt, Long resourceId) {
        User user = currentUser(jwt);
        UserSavedResource saved = userSavedResourceRepository
                .findByUserIdAndResourceId(user.getId(), resourceId)
                .orElseThrow(() -> AppException.notFound("Tài liệu chưa được lưu"));
        userSavedResourceRepository.delete(saved);
    }

    private String normalizeType(String type) {
        if (!StringUtils.hasText(type)) {
            return null;
        }

        String normalized = type.trim().toUpperCase(Locale.ROOT);
        if (!ALLOWED_TYPES.contains(normalized)) {
            throw AppException.badRequest("Loại tài liệu không hợp lệ");
        }
        return normalized;
    }

    private LearningResource getActiveResource(Long id) {
        return learningResourceRepository.findByIdAndStatus(id, ACTIVE)
                .orElseThrow(() -> AppException.notFound("Tài liệu không tồn tại"));
    }

    private User currentUser(Jwt jwt) {
        return userRepository.findByEmail(jwt.getSubject())
                .orElseThrow(() -> AppException.notFound("Người dùng không tồn tại"));
    }

    private Map<Long, UserSavedResource> savedByResourceId(Jwt jwt) {
        if (jwt == null || !StringUtils.hasText(jwt.getSubject())) {
            return Map.of();
        }

        return userRepository.findByEmail(jwt.getSubject())
                .map(user -> userSavedResourceRepository.findByUserIdOrderBySavedAtDesc(user.getId()).stream()
                        .collect(Collectors.toMap(
                                saved -> saved.getResource().getId(),
                                Function.identity(),
                                (left, right) -> left)))
                .orElseGet(Map::of);
    }

    private LearningResourceResponse toResponse(LearningResource resource, UserSavedResource saved) {
        return LearningResourceResponse.builder()
                .id(resource.getId())
                .careerId(resource.getCareer().getId())
                .careerName(resource.getCareer().getName())
                .title(resource.getTitle())
                .description(resource.getDescription())
                .resourceType(resource.getResourceType())
                .level(resource.getLevel())
                .url(resource.getUrl())
                .thumbnailUrl(resource.getThumbnailUrl())
                .status(resource.getStatus())
                .saved(saved != null)
                .savedAt(saved != null ? saved.getSavedAt() : null)
                .createdAt(resource.getCreatedAt())
                .build();
    }
}
