package com.example.production.controller;

import com.example.production.dto.ChatResponse;
import com.example.production.dto.ConversationRequestDTO;
import com.example.production.entity.Conversation;
import com.example.production.entity.User;
import com.example.production.exception.AppException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.production.repositpry.UserRepository;
import com.example.production.service.ChatService;

import java.security.Principal;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatRestController {

    private final ChatService chatService;
    private final UserRepository userRepository;

    @GetMapping("/history/{conversationId}")
    public ResponseEntity<ChatResponse> getHistory(
            @PathVariable Long conversationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Principal principal) {
        return ResponseEntity.ok(chatService.getMessageHistory(
                conversationId, page, size, principal.getName()));
    }

    @PostMapping("/conversation")
    public ResponseEntity<Conversation> getConversation(
            @Valid @RequestBody ConversationRequestDTO req, Principal principal) {
        User currentUser = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> AppException.notFound("Người dùng không tồn tại"));
        User otherUser = userRepository.findById(req.getMentorId())
                .orElseThrow(() -> AppException.notFound("Mentor không tồn tại"));

        Long p1 = Math.min(currentUser.getId(), otherUser.getId());
        Long p2 = Math.max(currentUser.getId(), otherUser.getId());

        return ResponseEntity.ok(chatService.getOrCreateConversation(p1, p2));
    }
}
