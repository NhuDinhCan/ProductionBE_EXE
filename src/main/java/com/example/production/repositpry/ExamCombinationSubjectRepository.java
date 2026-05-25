package com.example.production.repositpry;

import com.example.production.entity.ExamCombinationSubject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExamCombinationSubjectRepository extends JpaRepository<ExamCombinationSubject, Long> {

    @Query("""
            select ecs from ExamCombinationSubject ecs
            join fetch ecs.subject s
            where ecs.combination.id = :combinationId
            order by s.name asc
            """)
    List<ExamCombinationSubject> findByCombinationIdWithSubject(@Param("combinationId") Long combinationId);

    List<ExamCombinationSubject> findByCombinationId(Long combinationId);

    boolean existsByCombinationIdAndSubjectId(Long combinationId, Long subjectId);

    @Modifying
    void deleteByCombinationId(Long combinationId);
}
