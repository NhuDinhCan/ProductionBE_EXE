package com.example.production.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "university")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class University {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(name = "region")
    private String region;

    @Column(name = "type")
    private String type;

    @Column(name = "tuition_fee")
    private Double tuitionFee;
}
