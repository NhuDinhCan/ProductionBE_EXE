package com.example.production.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UserCreationResponse {
    private String email;
    private String firstName;
    private String lastName;
}
