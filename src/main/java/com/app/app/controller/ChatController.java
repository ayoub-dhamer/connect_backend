package com.app.app.controller;

import com.app.app.model.ChatMessage;
import com.app.app.repository.UserRepository;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Controller
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final UserRepository userRepository;

    public ChatController(SimpMessagingTemplate messagingTemplate, UserRepository userRepository) {
        this.messagingTemplate = messagingTemplate;
        this.userRepository = userRepository;
    }

    @MessageMapping("/chat") // Frontend sends here: /app/chat
    public void processMessage(@Payload ChatMessage chatMessage) {
        // Fill timestamp
        chatMessage.setTimestamp(
                LocalDateTime.parse(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
        );

        // Broadcast to receiver's queue
        messagingTemplate.convertAndSendToUser(
                chatMessage.getReceiver().getEmail(),
                "/queue/messages",
                chatMessage
        );

        // Optionally save message in DB
        // chatMessageRepository.save(chatMessage);
    }
}
