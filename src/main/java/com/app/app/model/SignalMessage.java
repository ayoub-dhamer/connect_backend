package com.app.app.model;

import lombok.Data;

import java.util.Map;

/**
 * Generic signaling message for WebRTC
 * type: "offer" | "answer" | "candidate" | "join" | "leave"
 * roomId: the room identifier
 * sender: sender id/email
 * payload: the SDP or ICE candidate object
 */
@Data
public class SignalMessage {
    private String type;
    private String roomId;
    private String sender;
    private Map<String, Object> payload; // contains 'sdp' or 'candidate' depending on type
}
