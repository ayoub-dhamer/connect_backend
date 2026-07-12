// CallInvite.java
package com.app.app.model;

public record CallInvite(
        String type,          // "video" | "audio"
        String roomId,
        String callerEmail,
        String callerName,
        String receiverEmail
) {}