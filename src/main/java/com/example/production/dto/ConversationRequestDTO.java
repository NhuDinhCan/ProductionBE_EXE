package com.example.production.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ConversationRequestDTO {
    private Long userId;

    @NotNull(message = "mentorId không được để trống")
    private Long mentorId;
}
