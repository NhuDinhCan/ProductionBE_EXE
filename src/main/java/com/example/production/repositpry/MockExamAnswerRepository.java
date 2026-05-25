package com.example.production.repositpry;

import com.example.production.entity.MockExamAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MockExamAnswerRepository extends JpaRepository<MockExamAnswer, Long> {

    @Query("""
            select a from MockExamAnswer a
            join fetch a.question q
            where a.attempt.id = :attemptId
            order by q.orderIndex asc, q.id asc
            """)
    List<MockExamAnswer> findByAttemptIdWithQuestions(@Param("attemptId") Long attemptId);

    @Modifying
    @Query("delete from MockExamAnswer a where a.attempt.id = :attemptId")
    void deleteByAttemptId(@Param("attemptId") Long attemptId);

    @Modifying
    @Query("delete from MockExamAnswer a where a.attempt.user.id = :userId")
    void deleteByAttemptUserId(@Param("userId") Long userId);
}
