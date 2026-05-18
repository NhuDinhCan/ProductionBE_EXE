package com.example.production.controller;


import com.example.production.dto.MessageDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import com.example.production.service.ChatService;
import com.example.production.service.OnlineUserService;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatService chatService;
    private final OnlineUserService onlineUserService;

    @MessageMapping("/chat.send")
    public void sendMessage(@Payload MessageDTO chatMessage, Principal principal) {
        if (principal == null) {
            throw new MessageDeliveryException("Vui lòng đăng nhập");
        }
        MessageDTO savedMsg = chatService.saveMessage(chatMessage, principal.getName());
        messagingTemplate.convertAndSend(
                "/topic/messages/" + savedMsg.getConversationId(), savedMsg);
        chatService.getParticipantEmails(savedMsg.getConversationId())
                .forEach(email -> messagingTemplate.convertAndSendToUser(
                        email, "/queue/messages", savedMsg));
    }

    @MessageMapping("/ping")
    public void ping(Principal principal) {
        if (principal != null) onlineUserService.userOnline(principal.getName(), "ping");
    }
}
