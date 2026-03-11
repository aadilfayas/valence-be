package com.valence.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public class MoodSessionHistoryResponse {

    private UUID sessionId;
    private double valence;
    private double arousal;
    private double goalValence;
    private double goalArousal;
    private LocalDateTime createdAt;

    public MoodSessionHistoryResponse(UUID sessionId,
                                      double valence,
                                      double arousal,
                                      double goalValence,
                                      double goalArousal,
                                      LocalDateTime createdAt) {
        this.sessionId = sessionId;
        this.valence = valence;
        this.arousal = arousal;
        this.goalValence = goalValence;
        this.goalArousal = goalArousal;
        this.createdAt = createdAt;
    }

    public UUID getSessionId() {
        return sessionId;
    }

    public void setSessionId(UUID sessionId) {
        this.sessionId = sessionId;
    }

    public double getValence() {
        return valence;
    }

    public void setValence(double valence) {
        this.valence = valence;
    }

    public double getArousal() {
        return arousal;
    }

    public void setArousal(double arousal) {
        this.arousal = arousal;
    }

    public double getGoalValence() {
        return goalValence;
    }

    public void setGoalValence(double goalValence) {
        this.goalValence = goalValence;
    }

    public double getGoalArousal() {
        return goalArousal;
    }

    public void setGoalArousal(double goalArousal) {
        this.goalArousal = goalArousal;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
