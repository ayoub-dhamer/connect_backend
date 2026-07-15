package com.app.app.controller;

import com.app.app.dto.GroupMessageDTO;
import com.app.app.mapper.CentralMapper;
import com.app.app.model.Group;
import com.app.app.model.GroupMessage;
import com.app.app.model.User;
import com.app.app.repository.GroupMessageRepository;
import com.app.app.repository.GroupRepository;
import com.app.app.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@Controller
public class GroupChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final GroupMessageRepository groupMessageRepository;
    private final CentralMapper centralMapper;

    public GroupChatController(SimpMessagingTemplate messagingTemplate,
                               GroupRepository groupRepository,
                               UserRepository userRepository,
                               GroupMessageRepository groupMessageRepository,
                               CentralMapper centralMapper) {
        this.messagingTemplate = messagingTemplate;
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
        this.groupMessageRepository = groupMessageRepository;
        this.centralMapper = centralMapper;
    }

    public record IncomingGroupMessage(String senderEmail, String content) {}

    @MessageMapping("/group-chat/{groupId}")
    public void sendGroupMessage(@DestinationVariable Long groupId, @Payload IncomingGroupMessage incoming) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new EntityNotFoundException("Group not found"));

        User sender = userRepository.findByEmail(incoming.senderEmail())
                .orElseThrow(() -> new EntityNotFoundException("Sender not found"));

        boolean isMember = group.getMembers().stream().anyMatch(m -> m.getId().equals(sender.getId()));
        if (!isMember) throw new AccessDeniedException("Not a member of this group");

        GroupMessage message = new GroupMessage();
        message.setGroup(group);
        message.setSender(sender);
        message.setContent(incoming.content());
        message.setTimestamp(LocalDateTime.now());

        groupMessageRepository.save(message);

        GroupMessageDTO dto = centralMapper.toDTO(message);
        messagingTemplate.convertAndSend("/topic/group." + groupId, dto);
    }
}

@RestController
@RequestMapping("/api/groups")
class GroupMessageHistoryController {

    private final GroupMessageRepository groupMessageRepository;
    private final GroupRepository groupRepository;
    private final CentralMapper centralMapper;

    GroupMessageHistoryController(GroupMessageRepository groupMessageRepository,
                                  GroupRepository groupRepository,
                                  CentralMapper centralMapper) {
        this.groupMessageRepository = groupMessageRepository;
        this.groupRepository = groupRepository;
        this.centralMapper = centralMapper;
    }

    @GetMapping("/{groupId}/messages")
    public List<GroupMessageDTO> getHistory(@PathVariable Long groupId, Authentication auth) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new EntityNotFoundException("Group not found"));

        boolean isMember = group.getMembers().stream().anyMatch(m -> m.getEmail().equals(auth.getName()));
        if (!isMember) throw new AccessDeniedException("Not a member of this group");

        return groupMessageRepository.findByGroupId(groupId)
                .stream()
                .map(centralMapper::toDTO)
                .toList();
    }
}