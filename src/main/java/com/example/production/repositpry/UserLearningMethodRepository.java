package com.example.production.repositpry;

import com.example.production.entity.UserLearningMethod;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserLearningMethodRepository extends JpaRepository<UserLearningMethod, Long> {

    @EntityGraph(attributePaths = {"career", "learningMethod"})
    List<UserLearningMethod> findByUserIdOrderByAppliedAtDesc(Long userId);

    @EntityGraph(attributePaths = {"career", "learningMethod"})
    List<UserLearningMethod> findByUserIdAndStatusOrderByAppliedAtDesc(Long userId, String status);

    @EntityGraph(attributePaths = {"career", "learningMethod"})
    List<UserLearningMethod> findByUserIdAndCareerIdOrderByAppliedAtDesc(Long userId, Long careerId);

    @EntityGraph(attributePaths = {"career", "learningMethod"})
    List<UserLearningMethod> findByUserIdAndCareerIdAndStatusOrderByAppliedAtDesc(Long userId, Long careerId, String status);

    @EntityGraph(attributePaths = {"career", "learningMethod"})
    Optional<UserLearningMethod> findByUserIdAndCareerIdAndLearningMethodId(
            Long userId,
            Long careerId,
            Long learningMethodId);

    boolean existsByUserIdAndCareerIdAndLearningMethodIdAndStatus(
            Long userId,
            Long careerId,
            Long learningMethodId,
            String status);
}


