// SignalMessage.java
package com.app.app.model;

import lombok.Data;
import java.util.Map;

@Data
public class SignalMessage {
    private String type;
    private String roomId;
    private String sender;
    private Map<String, Object> payload;
    private Boolean micOn;   // add this
    private Boolean camOn;   // add this
    private String text;
}