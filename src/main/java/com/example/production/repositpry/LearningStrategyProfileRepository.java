package com.example.production.repositpry;

import com.example.production.entity.LearningStrategyProfile;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LearningStrategyProfileRepository extends JpaRepository<LearningStrategyProfile, Long> {

    @EntityGraph(attributePaths = "career")
    Optional<LearningStrategyProfile> findByCareerId(Long careerId);
}
