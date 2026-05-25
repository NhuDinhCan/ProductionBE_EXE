package com.example.production.repositpry;

import com.example.production.entity.StudySchedule;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudyScheduleRepository extends JpaRepository<StudySchedule, Long> {

    @EntityGraph(attributePaths = {"career", "learningMethod"})
    List<StudySchedule> findByUserIdAndStatusOrderByStartTimeAsc(Long userId, String status);

    @EntityGraph(attributePaths = {"career", "learningMethod"})
    List<StudySchedule> findByUserIdAndDayOfWeekAndStatusOrderByStartTimeAsc(
            Long userId,
            String dayOfWeek,
            String status);

    @EntityGraph(attributePaths = {"career", "learningMethod"})
    List<StudySchedule> findByUserIdAndCareerIdAndLearningMethodIdAndStatus(
            Long userId,
            Long careerId,
            Long learningMethodId,
            String status);

    @EntityGraph(attributePaths = {"career", "learningMethod"})
    Optional<StudySchedule> findByIdAndUserId(Long id, Long userId);
    @EntityGraph(attributePaths = {"career", "learningMethod"})
    List<StudySchedule> findByUserIdAndCareerIdAndStatus(
            Long userId,
            Long careerId,
            String status
    );


}

