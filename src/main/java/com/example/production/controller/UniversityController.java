package com.example.production.controller;


import com.example.production.dto.UniversityRequestDTO;
import com.example.production.dto.UniversityResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.production.service.UniversityService;

import java.util.List;

@RestController
@RequestMapping("/api/university")
@RequiredArgsConstructor
public class UniversityController {

    private final UniversityService universityService;

    @PostMapping("/suggest")
    public List<UniversityResponseDTO> suggestUniversity(
            @Valid @RequestBody UniversityRequestDTO request
    ){
        return universityService.suggestUniversity(request);
    }
}
