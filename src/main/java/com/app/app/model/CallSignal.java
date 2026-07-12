// CallSignal.java — new file, replaces the one-way CallInvite
package com.app.app.model;

public record CallSignal(
        String type,           // "invite" | "accept" | "decline" | "cancel" | "hangup"
        String callId,         // unique per call attempt, not the roomId
        String roomId,
        String callType,       // "video" | "audio"
        String callerEmail,
        String callerName,
        String receiverEmail,
        String startedAt
) {}