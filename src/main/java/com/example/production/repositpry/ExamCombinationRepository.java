package com.example.production.repositpry;

import com.example.production.entity.ExamCombination;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExamCombinationRepository extends JpaRepository<ExamCombination, Long> {
    List<ExamCombination> findAllByOrderByCodeAsc();
}
