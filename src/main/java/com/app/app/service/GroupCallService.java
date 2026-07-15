package com.app.app.service;

import com.app.app.model.*;
import com.app.app.repository.*;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class GroupCallService {

    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final GroupCallSessionRepository sessionRepository;
    private final GroupCallParticipantRepository participantRepository;
    private final SimpUserRegistry simpUserRegistry;

    public GroupCallService(GroupRepository groupRepository,
                            UserRepository userRepository,
                            GroupCallSessionRepository sessionRepository,
                            GroupCallParticipantRepository participantRepository,
                            SimpUserRegistry simpUserRegistry) {
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
        this.sessionRepository = sessionRepository;
        this.participantRepository = participantRepository;
        this.simpUserRegistry = simpUserRegistry;
    }

    /**
     * Creates the call session and, for every group member other than the
     * caller, records whether they were reachable (had an active WS session)
     * at invite time. Reachable members start as NO_ANSWER (pending);
     * unreachable ones are immediately marked MISSED.
     *
     * Returns the list of member emails that WERE reachable, so the caller
     * can be told who actually got rung.
     */
    @Transactional
    public List<String> startGroupCall(String callId, Long groupId, String callerEmail, String callType) {
        Group group = groupRepository.findByIdWithMembers(groupId)
                .orElseThrow(() -> new EntityNotFoundException("Group not found"));
        User caller = userRepository.findByEmail(callerEmail)
                .orElseThrow(() -> new EntityNotFoundException("Caller not found"));

        GroupCallSession session = new GroupCallSession();
        session.setCallId(callId);
        session.setGroup(group);
        session.setStartedBy(caller);
        session.setCallType(CallType.valueOf(callType.toUpperCase()));
        sessionRepository.save(session);

        List<String> reachable = new java.util.ArrayList<>();

        for (User member : group.getMembers()) {
            if (member.getEmail().equals(callerEmail)) continue;

            boolean online = simpUserRegistry.getUser(member.getEmail()) != null;

            GroupCallParticipant participant = new GroupCallParticipant();
            participant.setCall(session);
            participant.setUser(member);
            participant.setOutcome(online ? ParticipantOutcome.NO_ANSWER : ParticipantOutcome.MISSED);
            participantRepository.save(participant);

            if (online) reachable.add(member.getEmail());
        }

        // Caller themself always counts as JOINED.
        GroupCallParticipant callerParticipant = new GroupCallParticipant();
        callerParticipant.setCall(session);
        callerParticipant.setUser(caller);
        callerParticipant.setOutcome(ParticipantOutcome.JOINED);
        callerParticipant.setRespondedAt(LocalDateTime.now());
        participantRepository.save(callerParticipant);

        return reachable;
    }

    @Transactional
    public void recordOutcome(String callId, String userEmail, ParticipantOutcome outcome) {
        participantRepository.findByCallIdAndUserEmail(callId, userEmail).ifPresent(p -> {
            p.setOutcome(outcome);
            p.setRespondedAt(LocalDateTime.now());
            participantRepository.save(p);
        });
    }

    @Transactional
    public void endCall(String callId) {
        sessionRepository.findById(callId).ifPresent(s -> {
            s.setEndedAt(LocalDateTime.now());
            sessionRepository.save(s);
        });
    }

    @Transactional
    public List<String> cancelPendingInvites(String callId) {
        List<GroupCallParticipant> pending = participantRepository.findByCallId(callId).stream()
                .filter(p -> p.getOutcome() == ParticipantOutcome.NO_ANSWER)
                .toList();

        List<String> emails = new java.util.ArrayList<>();
        for (GroupCallParticipant p : pending) {
            p.setOutcome(ParticipantOutcome.CANCELLED);
            p.setRespondedAt(LocalDateTime.now());
            participantRepository.save(p);
            emails.add(p.getUser().getEmail());
        }
        return emails;
    }

    @Transactional
    public boolean recordOutcomeAndCheckAllResolved(String callId, String userEmail, ParticipantOutcome outcome) {
        participantRepository.findByCallIdAndUserEmail(callId, userEmail).ifPresent(p -> {
            p.setOutcome(outcome);
            p.setRespondedAt(LocalDateTime.now());
            participantRepository.save(p);
        });

        // "Resolved" means no one is still sitting at NO_ANSWER (i.e. still ringing).
        return participantRepository.findByCallId(callId).stream()
                .noneMatch(p -> p.getOutcome() == ParticipantOutcome.NO_ANSWER);
    }
}