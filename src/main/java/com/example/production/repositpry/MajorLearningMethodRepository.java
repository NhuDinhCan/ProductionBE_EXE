package com.example.production.repositpry;

import com.example.production.entity.MajorLearningMethod;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MajorLearningMethodRepository extends JpaRepository<MajorLearningMethod, Long> {

    @EntityGraph(attributePaths = {"career", "learningMethod"})
    List<MajorLearningMethod> findByCareerIdOrderBySortOrderAsc(Long careerId);

    boolean existsByCareerIdAndLearningMethodId(Long careerId, Long learningMethodId);
}
