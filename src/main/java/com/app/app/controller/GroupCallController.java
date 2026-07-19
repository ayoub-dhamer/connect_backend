package com.app.app.controller;

import com.app.app.model.GroupCallSignal;
import com.app.app.model.GroupMembership;
import com.app.app.model.ParticipantOutcome;
import com.app.app.model.User;
import com.app.app.repository.GroupMembershipRepository;
import com.app.app.service.GroupCallService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class GroupCallController {

    private final SimpMessagingTemplate messagingTemplate;
    private final GroupMembershipRepository membershipRepository;
    private final GroupCallService groupCallService;

    public GroupCallController(SimpMessagingTemplate messagingTemplate,
                               GroupMembershipRepository membershipRepository,
                               GroupCallService groupCallService) {
        this.messagingTemplate = messagingTemplate;
        this.membershipRepository = membershipRepository;
        this.groupCallService = groupCallService;
    }

    @MessageMapping("/group-call/signal")
    public void relayGroupSignal(@Payload GroupCallSignal signal) {
        switch (signal.type()) {

            case "invite" -> {
                groupCallService.startGroupCall(signal.callId(), signal.groupId(), signal.callerEmail(), signal.callType());

                List<User> members = membershipRepository.findByGroupIdWithUser(signal.groupId()).stream()
                        .map(GroupMembership::getUser)
                        .toList();

                for (User member : members) {
                    if (member.getEmail().equals(signal.callerEmail())) continue;
                    messagingTemplate.convertAndSendToUser(member.getEmail(), "/queue/group-call-signal", signal);
                }
            }

            case "accept" -> {
                groupCallService.recordOutcomeAndCheckAllResolved(
                        signal.callId(), signal.respondentEmail(), ParticipantOutcome.JOINED);
                messagingTemplate.convertAndSendToUser(signal.callerEmail(), "/queue/group-call-signal", signal);
            }

            case "decline" -> {
                boolean allResolved = groupCallService.recordOutcomeAndCheckAllResolved(
                        signal.callId(), signal.respondentEmail(), ParticipantOutcome.DECLINED);

                messagingTemplate.convertAndSendToUser(signal.callerEmail(), "/queue/group-call-signal", signal);

                if (allResolved) {
                    groupCallService.endCall(signal.callId());
                    messagingTemplate.convertAndSendToUser(
                            signal.callerEmail(),
                            "/queue/group-call-signal",
                            new GroupCallSignal("all-declined", signal.callId(), signal.roomId(), signal.callType(),
                                    signal.callerEmail(), signal.callerName(), signal.groupId(), signal.groupName(),
                                    null, null)
                    );
                }
            }

            case "cancel" -> {
                List<String> stillRinging = groupCallService.cancelPendingInvites(signal.callId());
                for (String email : stillRinging) {
                    messagingTemplate.convertAndSendToUser(email, "/queue/group-call-signal", signal);
                }
                groupCallService.endCall(signal.callId());
            }

            case "ended" -> groupCallService.endCall(signal.callId());

            default -> { }
        }
    }
}