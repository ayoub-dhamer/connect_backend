package com.app.app.controller;

import com.app.app.dto.GroupCallSessionDTO;
import com.app.app.dto.ParticipantOutcomeDTO;
import com.app.app.mapper.CentralMapper;
import com.app.app.repository.GroupCallParticipantRepository;
import com.app.app.repository.GroupCallSessionRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/groups")
public class GroupCallHistoryController {

    private final GroupCallSessionRepository sessionRepository;
    private final GroupCallParticipantRepository participantRepository;
    private final CentralMapper centralMapper;

    public GroupCallHistoryController(GroupCallSessionRepository sessionRepository,
                                      GroupCallParticipantRepository participantRepository,
                                      CentralMapper centralMapper) {
        this.sessionRepository = sessionRepository;
        this.participantRepository = participantRepository;
        this.centralMapper = centralMapper;
    }

    @GetMapping("/{groupId}/calls")
    public List<GroupCallSessionDTO> getCallHistory(@PathVariable Long groupId) {
        return sessionRepository.findByGroupId(groupId).stream().map(session -> {
            List<ParticipantOutcomeDTO> participants = participantRepository
                    .findByCallId(session.getCallId())
                    .stream()
                    .map(centralMapper::toDTO)
                    .toList();

            GroupCallSessionDTO base = centralMapper.toDTO(session);
            return new GroupCallSessionDTO(
                    base.callId(), base.groupId(), base.startedByEmail(), base.startedByName(),
                    base.callType(), base.startedAt(), base.endedAt(), participants
            );
        }).toList();
    }
}