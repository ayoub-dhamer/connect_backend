// ChatController.java (STOMP only)
package com.app.app.controller;

import com.app.app.dto.ChatMessageDTO;
import com.app.app.mapper.CentralMapper;
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
    private final ChatMessageRepository chatMessageRepository;
    private final CentralMapper centralMapper;

    public ChatMessageController(SimpMessagingTemplate messagingTemplate,
                          UserRepository userRepository,
                          ChatMessageRepository chatMessageRepository,
                          CentralMapper centralMapper) {
        this.messagingTemplate = messagingTemplate;
        this.userRepository = userRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.centralMapper = centralMapper;
    }

    @MessageMapping("/chat")
    public void processMessage(@Payload ChatMessageDTO incoming) {
        User sender = userRepository.findByEmail(incoming.senderEmail())
                .orElseThrow(() -> new EntityNotFoundException("Sender not found"));
        User receiver = userRepository.findByEmail(incoming.receiverEmail())
                .orElseThrow(() -> new EntityNotFoundException("Receiver not found"));

        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setSender(sender);
        chatMessage.setReceiver(receiver);
        chatMessage.setContent(incoming.content());
        chatMessage.setTimestamp(LocalDateTime.now());

        chatMessageRepository.save(chatMessage);

        ChatMessageDTO outgoing = centralMapper.toDTO(chatMessage);

        messagingTemplate.convertAndSendToUser(
                receiver.getEmail(),
                "/queue/messages",
                outgoing
        );
    }
}