package com.example.production.dto;

import lombok.Data;

@Data
public class LogoutRequest {
    private String refreshToken;
}
