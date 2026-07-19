package com.app.app.controller;

import com.app.app.dto.GroupMessageDTO;
import com.app.app.mapper.CentralMapper;
import com.app.app.model.Group;
import com.app.app.model.GroupMessage;
import com.app.app.model.User;
import com.app.app.repository.GroupMembershipRepository;
import com.app.app.repository.GroupMessageRepository;
import com.app.app.repository.GroupRepository;
import com.app.app.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;

@Controller
public class GroupChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final GroupRepository groupRepository;
    private final GroupMembershipRepository membershipRepository;
    private final UserRepository userRepository;
    private final GroupMessageRepository groupMessageRepository;
    private final CentralMapper centralMapper;

    public GroupChatController(SimpMessagingTemplate messagingTemplate,
                               GroupRepository groupRepository,
                               GroupMembershipRepository membershipRepository,
                               UserRepository userRepository,
                               GroupMessageRepository groupMessageRepository,
                               CentralMapper centralMapper) {
        this.messagingTemplate = messagingTemplate;
        this.groupRepository = groupRepository;
        this.membershipRepository = membershipRepository;
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

        boolean isMember = membershipRepository.findByGroupIdWithUser(groupId).stream()
                .anyMatch(m -> m.getUser().getId().equals(sender.getId()));
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