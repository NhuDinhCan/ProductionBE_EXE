package com.example.production.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "university_major")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UniversityMajor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "university_id")
    private University university;

    @ManyToOne
    @JoinColumn(name = "career_id")
    private Career career;

    @Column(name = "score_required")
    private Double scoreRequired;
}
