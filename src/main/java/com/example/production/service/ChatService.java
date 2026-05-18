package com.example.production.service;

import com.example.production.dto.ChatResponse;
import com.example.production.dto.MessageDTO;
import com.example.production.entity.Conversation;
import com.example.production.entity.Message;
import com.example.production.entity.User;
import com.example.production.exception.AppException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.production.repositpry.ConversationRepository;
import com.example.production.repositpry.MessageRepository;
import com.example.production.repositpry.UserRepository;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;

    public MessageDTO saveMessage(MessageDTO dto, String senderEmail) {
        if (dto.getConversationId() == null) {
            throw AppException.badRequest("conversationId không được để trống");
        }
        if (!StringUtils.hasText(dto.getContent())) {
            throw AppException.badRequest("Nội dung tin nhắn không được để trống");
        }

        User sender = getUserByEmail(senderEmail);
        authorizeParticipant(dto.getConversationId(), sender.getId());

        Message message = Message.builder()
                .conversationId(dto.getConversationId())
                .senderEmail(sender.getEmail())
                .content(dto.getContent().trim())
                .createdAt(LocalDateTime.now())
                .build();
        Message saved = messageRepository.save(message);
        dto.setId(saved.getId());
        dto.setSenderEmail(saved.getSenderEmail());
        dto.setContent(saved.getContent());
        dto.setCreatedAt(saved.getCreatedAt());
        return dto;
    }

    public ChatResponse getMessageHistory(Long conversationId, int page, int size, String requesterEmail) {
        if (conversationId == null) {
            throw AppException.badRequest("conversationId không được để trống");
        }

        User requester = getUserByEmail(requesterEmail);
        authorizeParticipant(conversationId, requester.getId());

        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);
        Pageable pageable = PageRequest.of(safePage, safeSize);
        Slice<Message> slice = messageRepository
                .findByConversationIdOrderByCreatedAtDesc(conversationId, pageable);

        List<MessageDTO> dtos = slice.getContent().stream()
                .map(m -> MessageDTO.builder()
                        .id(m.getId())
                        .conversationId(m.getConversationId())
                        .content(m.getContent())
                        .senderEmail(m.getSenderEmail())
                        .createdAt(m.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        return ChatResponse.builder()
                .messages(dtos)
                .hasNext(slice.hasNext())
                .build();
    }

    public void authorizeConversationAccess(Long conversationId, String requesterEmail) {
        if (conversationId == null) {
            throw AppException.badRequest("conversationId không được để trống");
        }

        User requester = getUserByEmail(requesterEmail);
        authorizeParticipant(conversationId, requester.getId());
    }

    public List<String> getParticipantEmails(Long conversationId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> AppException.notFound("Cuoc tro chuyen khong ton tai"));
        User user = userRepository.findById(conversation.getUserId())
                .orElseThrow(() -> AppException.notFound("Nguoi dung khong ton tai"));
        User mentor = userRepository.findById(conversation.getMentorId())
                .orElseThrow(() -> AppException.notFound("Nguoi dung khong ton tai"));

        return List.of(user.getEmail(), mentor.getEmail()).stream()
                .distinct()
                .toList();
    }

    @Transactional
    public Conversation getOrCreateConversation(Long p1, Long p2) {
        return conversationRepository.findFirstByUserIdAndMentorId(p1, p2)
                .orElseGet(() -> conversationRepository.save(
                        Conversation.builder()
                                .userId(p1)
                                .mentorId(p2)
                                .createdAt(LocalDateTime.now())
                                .build()
                ));
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> AppException.notFound("Người dùng không tồn tại"));
    }

    private Conversation authorizeParticipant(Long conversationId, Long userId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> AppException.notFound("Cuộc trò chuyện không tồn tại"));

        boolean isParticipant = userId.equals(conversation.getUserId())
                || userId.equals(conversation.getMentorId());
        if (!isParticipant) {
            throw AppException.forbidden("Bạn không có quyền truy cập cuộc trò chuyện này");
        }

        return conversation;
    }
}
