package com.app.app.controller;

import com.app.app.model.CallSignal;
import com.app.app.service.CallStateService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class CallController {

    private final SimpMessagingTemplate messagingTemplate;
    private final CallStateService callStateService;

    public CallController(SimpMessagingTemplate messagingTemplate, CallStateService callStateService) {
        this.messagingTemplate = messagingTemplate;
        this.callStateService = callStateService;
    }

    @MessageMapping("/call/signal")
    public void relaySignal(@Payload CallSignal signal) {
        switch (signal.type()) {
            case "invite" -> {
                boolean registered = callStateService.registerRinging(
                        signal.callId(), signal.callerEmail(), signal.receiverEmail());

                if (!registered) {
                    // Receiver (or caller) already tied up — bounce it back as "busy"
                    // instead of forwarding the invite.
                    messagingTemplate.convertAndSendToUser(
                            signal.callerEmail(),
                            "/queue/call-signal",
                            new CallSignal("busy", signal.callId(), signal.roomId(), signal.callType(),
                                    signal.callerEmail(), signal.callerName(), signal.callerEmail(), null)
                    );
                    return;
                }
            }
            case "decline", "cancel", "ended" -> callStateService.clear(signal.callId());
            // "accept" needs no state change — registerRinging already marked both busy.
            default -> { /* no-op for other types */ }
        }

        messagingTemplate.convertAndSendToUser(signal.receiverEmail(), "/queue/call-signal", signal);
    }
}