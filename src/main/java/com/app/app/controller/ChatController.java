package com.app.app.controller;

import com.app.app.model.ChatMessage;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;

@Controller
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;
    // Note: If you add persistence later, inject ChatMessageRepository here.

    public ChatController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/chat")
    public void processMessage(@Payload ChatMessage chatMessage) {
        // 1. Efficient Timestamping
        // Removed the expensive String parsing. LocalDateTime.now() is sufficient.
        chatMessage.setTimestamp(LocalDateTime.now());

        // 2. Routing Logic
        // We use the receiver's email (from the payload) to route the message.
        // Ensure your Angular client subscribes to /user/queue/messages
        messagingTemplate.convertAndSendToUser(
                chatMessage.getReceiver().getEmail(),
                "/queue/messages",
                chatMessage
        );

        // 3. Recommended: Persistence
        // In a production app, you'd save this to PostgreSQL here:
        // chatMessageRepository.save(chatMessage);
    }
}