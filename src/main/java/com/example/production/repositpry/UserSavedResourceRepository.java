package com.example.production.repositpry;

import com.example.production.entity.UserSavedResource;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserSavedResourceRepository extends JpaRepository<UserSavedResource, Long> {

    @EntityGraph(attributePaths = {"resource", "resource.career"})
    List<UserSavedResource> findByUserIdOrderBySavedAtDesc(Long userId);

    Optional<UserSavedResource> findByUserIdAndResourceId(Long userId, Long resourceId);

    boolean existsByUserIdAndResourceId(Long userId, Long resourceId);
}
