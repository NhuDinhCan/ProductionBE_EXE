package com.example.production.repositpry;


import com.example.production.entity.UniversityMajor;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UniversityMajorRepository extends JpaRepository<UniversityMajor,Long> {
    List<UniversityMajor> findByCareerIdAndScoreRequiredLessThanEqual(Long careerId, Double required);
    List<UniversityMajor> findByCareerIdAndScoreRequiredLessThanEqualOrderByScoreRequiredDesc(
            Long careerId,
            Double score
    );

    @EntityGraph(attributePaths = {"university", "career"})
    List<UniversityMajor> findTop8ByCareerIdOrderByScoreRequiredDesc(Long careerId);
}
