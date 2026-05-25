package com.example.production.repositpry;

import com.example.production.entity.LearningMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LearningMethodRepository extends JpaRepository<LearningMethod, Long> {
}
