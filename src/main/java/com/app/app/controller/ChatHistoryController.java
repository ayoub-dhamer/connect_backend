package com.app.app.controller;

import com.app.app.dto.ChatMessageDTO;
import com.app.app.mapper.CentralMapper;
import com.app.app.repository.ChatMessageRepository;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
public class ChatHistoryController {

    private final ChatMessageRepository chatMessageRepository;
    private final CentralMapper centralMapper;

    public ChatHistoryController(ChatMessageRepository chatMessageRepository, CentralMapper centralMapper) {
        this.chatMessageRepository = chatMessageRepository;
        this.centralMapper = centralMapper;
    }

    @GetMapping("/history/{contactEmail}")
    public List<ChatMessageDTO> getHistory(@PathVariable String contactEmail, Authentication auth) {
        return chatMessageRepository.findConversation(auth.getName(), contactEmail)
                .stream()
                .map(centralMapper::toDTO)
                .toList();
    }
}