package com.example.production.repositpry;

import com.example.production.entity.MockExamAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

@Repository
public interface MockExamAttemptRepository extends JpaRepository<MockExamAttempt, Long> {
    List<MockExamAttempt> findByUserIdOrderByStartedAtDesc(Long userId);

    Optional<MockExamAttempt> findByIdAndUserId(Long id, Long userId);

    boolean existsByUserIdAndExamId(Long userId, Long examId);

    @Modifying
    @Query("delete from MockExamAttempt a where a.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);

    List<MockExamAttempt> findTop5ByOrderByStartedAtDesc();
}
