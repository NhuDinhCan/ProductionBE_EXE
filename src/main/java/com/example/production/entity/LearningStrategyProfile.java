package com.example.production.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "learning_strategy_profiles",
        uniqueConstraints = @UniqueConstraint(columnNames = "career_id")
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LearningStrategyProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "career_id", nullable = false)
    private Career career;

    @Column(nullable = false, length = 2000)
    private String description;

    @Column(length = 2000)
    private String skills;

    @Column(length = 2000)
    private String tools;

    @Column(name = "weekly_roadmap", length = 4000)
    private String weeklyRoadmap;
}
