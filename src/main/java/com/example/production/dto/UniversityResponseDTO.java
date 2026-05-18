package com.example.production.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UniversityResponseDTO {

    private String universityName;
    private String careerName;
    private Double scoreRequired;
    private String region;
    private String type;
    private Double tuitionFee;
}
