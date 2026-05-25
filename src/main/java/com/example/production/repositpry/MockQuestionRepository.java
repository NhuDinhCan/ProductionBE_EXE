package com.example.production.repositpry;

import com.example.production.entity.MockQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MockQuestionRepository extends JpaRepository<MockQuestion, Long> {
    List<MockQuestion> findByExamIdOrderByOrderIndexAscIdAsc(Long examId);

    long countByExamId(Long examId);

    boolean existsByExamIdAndOrderIndex(Long examId, Integer orderIndex);

    boolean existsByExamIdAndOrderIndexAndIdNot(Long examId, Integer orderIndex, Long id);

    Optional<MockQuestion> findByExamIdAndOrderIndex(Long examId, Integer orderIndex);
}
