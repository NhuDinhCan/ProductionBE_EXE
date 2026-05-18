package com.example.production.repositpry;


import com.example.production.entity.QuestionCareerWeight;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionCareerWeightRepository extends JpaRepository<QuestionCareerWeight,Long> {

    List<QuestionCareerWeight> findByQuestionIdIn(List<Long> questionIds);
}
