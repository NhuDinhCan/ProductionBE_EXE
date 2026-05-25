package com.example.production.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "mock_questions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MockQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_id", nullable = false)
    private MockExam exam;

    @Lob
    @Column(name = "question_text", nullable = false)
    private String questionText;

    @Column(name = "option_a", nullable = false, length = 1000)
    private String optionA;

    @Column(name = "option_b", nullable = false, length = 1000)
    private String optionB;

    @Column(name = "option_c", nullable = false, length = 1000)
    private String optionC;

    @Column(name = "option_d", nullable = false, length = 1000)
    private String optionD;

    @Column(name = "correct_option", length = 1)
    private String correctOption;

    @Lob
    private String explanation;

    @Column(name = "order_index")
    private Integer orderIndex;

    @Column(name = "image_url", length = 1000)
    private String imageUrl;
}
