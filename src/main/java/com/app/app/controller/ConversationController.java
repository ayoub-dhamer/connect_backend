// controller/ConversationController.java
package com.app.app.controller;

import com.app.app.dto.CallLogDTO;
import com.app.app.dto.ChatMessageDTO;
import com.app.app.dto.TimelineItemDTO;
import com.app.app.mapper.CentralMapper;
import com.app.app.repository.CallLogRepository;
import com.app.app.repository.ChatMessageRepository;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Stream;

@RestController
@RequestMapping("/api/conversation")
public class ConversationController {

    private final ChatMessageRepository chatMessageRepository;
    private final CallLogRepository callLogRepository;
    private final CentralMapper centralMapper;

    public ConversationController(ChatMessageRepository chatMessageRepository,
                                  CallLogRepository callLogRepository,
                                  CentralMapper centralMapper) {
        this.chatMessageRepository = chatMessageRepository;
        this.callLogRepository = callLogRepository;
        this.centralMapper = centralMapper;
    }

    @GetMapping("/{contactEmail}")
    public List<TimelineItemDTO> getTimeline(@PathVariable String contactEmail, Authentication auth) {
        Stream<TimelineItemDTO> messages = chatMessageRepository
                .findConversation(auth.getName(), contactEmail)
                .stream()
                .map(centralMapper::toDTO)
                .map(m -> new TimelineItemDTO("message", m.timestamp(), m, null));

        Stream<TimelineItemDTO> calls = callLogRepository
                .findConversation(auth.getName(), contactEmail)
                .stream()
                .map(centralMapper::toDTO)
                .map(c -> new TimelineItemDTO("call", c.startedAt(), null, c));

        return Stream.concat(messages, calls)
                .sorted((a, b) -> a.timestamp().compareTo(b.timestamp()))
                .toList();
    }
}