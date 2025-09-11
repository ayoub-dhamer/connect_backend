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

    // Client will send to "/app/room/{roomId}"
    @MessageMapping("/room/{roomId}")
    public void signaling(@DestinationVariable String roomId, SignalMessage message) {
        // broadcast to topic/room.{roomId}
        template.convertAndSend("/topic/room." + roomId, message);
    }
}
