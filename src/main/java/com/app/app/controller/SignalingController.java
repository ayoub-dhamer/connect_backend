package com.app.app.controller;

import com.app.app.model.SignalMessage;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class SignalingController {

    private final SimpMessagingTemplate template;

    public SignalingController(SimpMessagingTemplate template) {
        this.template = template;
    }

    /**
     * Handles WebRTC signaling for room-based communication.
     * Path: /app/room/{roomId}
     */
    @MessageMapping("/room/{roomId}")
    public void signaling(@DestinationVariable String roomId, SignalMessage message) {
        // 1. Validation (Optional but Recommended)
        if (message == null || message.getType() == null) {
            return;
        }

        // 2. Broadcast to all participants in the specific room
        // The Angular client must subscribe to: /topic/room.{roomId}
        template.convertAndSend("/topic/room." + roomId, message);
    }
}