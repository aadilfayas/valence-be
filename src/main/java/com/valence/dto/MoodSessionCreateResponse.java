package com.valence.dto;

import java.util.UUID;

public class MoodSessionCreateResponse {

    private UUID sessionId;

    public MoodSessionCreateResponse(UUID sessionId) {
        this.sessionId = sessionId;
    }

    public UUID getSessionId() {
        return sessionId;
    }

    public void setSessionId(UUID sessionId) {
        this.sessionId = sessionId;
    }
}
