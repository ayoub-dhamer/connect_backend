package com.app.app.controller;

import com.app.app.dto.GroupMessageDTO;
import com.app.app.mapper.CentralMapper;
import com.app.app.repository.GroupMembershipRepository;
import com.app.app.repository.GroupMessageRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/groups")
class GroupMessageHistoryController {

    private final GroupMessageRepository groupMessageRepository;
    private final GroupMembershipRepository membershipRepository;
    private final CentralMapper centralMapper;

    GroupMessageHistoryController(GroupMessageRepository groupMessageRepository,
                                  GroupMembershipRepository membershipRepository,
                                  CentralMapper centralMapper) {
        this.groupMessageRepository = groupMessageRepository;
        this.membershipRepository = membershipRepository;
        this.centralMapper = centralMapper;
    }

    @GetMapping("/{groupId}/messages")
    public List<GroupMessageDTO> getHistory(@PathVariable Long groupId, Authentication auth) {
        boolean isMember = membershipRepository.findByGroupId(groupId).stream()
                .anyMatch(m -> m.getUser().getEmail().equals(auth.getName()));
        if (!isMember) throw new AccessDeniedException("Not a member of this group");

        return groupMessageRepository.findByGroupId(groupId)
                .stream()
                .map(centralMapper::toDTO)
                .toList();
    }
}