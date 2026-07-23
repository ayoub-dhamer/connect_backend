package com.app.app.controller;

import com.app.app.dto.ActiveGroupCallDTO;
import com.app.app.dto.PendingInviteDTO;
import com.app.app.model.GroupCallSession;
import com.app.app.repository.GroupCallParticipantRepository;
import com.app.app.repository.GroupCallSessionRepository;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/groups")
public class GroupCallStatusController {

    private final GroupCallSessionRepository sessionRepository;
    private final GroupCallParticipantRepository groupCallParticipantRepository;

    public GroupCallStatusController(GroupCallSessionRepository sessionRepository, GroupCallParticipantRepository groupCallParticipantRepository) {
        this.sessionRepository = sessionRepository;
        this.groupCallParticipantRepository = groupCallParticipantRepository;
    }

    @GetMapping("/{groupId}/active-call")
    public Optional<ActiveGroupCallDTO> getActiveCall(@PathVariable Long groupId) {
        List<GroupCallSession> active = sessionRepository.findActiveByGroupId(groupId);
        return active.stream().findFirst().map(s -> new ActiveGroupCallDTO(s.getCallId(), s.getRoomId(), s.getGroup().getId(), s.getCallType().name(), s.getStartedAt())
        );
    }

    @GetMapping("/pending-invites")
    public List<PendingInviteDTO> getPendingInvites(Authentication auth) {
        return groupCallParticipantRepository.findPendingByUserEmail(auth.getName()).stream()
                .map(p -> {
                    GroupCallSession s = p.getCall();
                    return new PendingInviteDTO(
                            s.getCallId(), s.getRoomId(), s.getGroup().getId(), s.getGroup().getName(),
                            s.getCallType().name(), s.getStartedBy().getEmail(), s.getStartedBy().getName()
                    );
                })
                .toList();
    }
}