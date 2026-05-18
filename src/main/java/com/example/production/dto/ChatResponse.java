package com.example.production.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ChatResponse {
    private List<MessageDTO> messages;
    private int totalPages;
    private long totalElements;
    private boolean hasNext;
}
