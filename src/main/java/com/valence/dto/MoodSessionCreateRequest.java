package com.valence.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public class MoodSessionCreateRequest {

    @NotNull
    private UUID userId;

    @NotBlank
    private String feelingMessage;

    @NotBlank
    private String triggerMessage;

    @NotBlank
    private String goalMessage;

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getFeelingMessage() {
        return feelingMessage;
    }

    public void setFeelingMessage(String feelingMessage) {
        this.feelingMessage = feelingMessage;
    }

    public String getTriggerMessage() {
        return triggerMessage;
    }

    public void setTriggerMessage(String triggerMessage) {
        this.triggerMessage = triggerMessage;
    }

    public String getGoalMessage() {
        return goalMessage;
    }

    public void setGoalMessage(String goalMessage) {
        this.goalMessage = goalMessage;
    }
}
