package com.example.production.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ConversationDTO {
    private Long id;
    private Long userId;
    private Long mentorId;
}
