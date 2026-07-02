package com.app.app.controller;

import com.app.app.model.ChatMessage;
import com.app.app.model.User;
import com.app.app.repository.ChatMessageRepository;
import com.app.app.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;

@Controller
public class ChatMessageController {

    private final SimpMessagingTemplate messagingTemplate;
    private final UserRepository userRepository;
    private final ChatMessageRepository chatMessageRepository; // add this repo

    public ChatMessageController(SimpMessagingTemplate messagingTemplate,
                                 UserRepository userRepository,
                                 ChatMessageRepository chatMessageRepository) {
        this.messagingTemplate = messagingTemplate;
        this.userRepository = userRepository;
        this.chatMessageRepository = chatMessageRepository;
    }

    @MessageMapping("/chat")
    public void processMessage(@Payload ChatMessage incoming) {
        User sender = userRepository.findByEmail(incoming.getSender().getEmail())
                .orElseThrow(() -> new EntityNotFoundException("Sender not found"));
        User receiver = userRepository.findByEmail(incoming.getReceiver().getEmail())
                .orElseThrow(() -> new EntityNotFoundException("Receiver not found"));

        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setSender(sender);
        chatMessage.setReceiver(receiver);
        chatMessage.setContent(incoming.getContent());
        chatMessage.setTimestamp(LocalDateTime.now());

        chatMessageRepository.save(chatMessage);

        messagingTemplate.convertAndSendToUser(
                receiver.getEmail(),
                "/queue/messages",
                chatMessage
        );
    }
}