package com.example.production.dto;

import lombok.Data;

@Data
public class ChatMessageDTO {
    private Long conversationId;
    private String receiverEmail;
    private String content;
}
