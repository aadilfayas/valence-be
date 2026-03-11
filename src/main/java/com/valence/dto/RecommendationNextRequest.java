package com.valence.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public class RecommendationNextRequest {

    @NotNull
    private UUID sessionId;

    @NotNull
    @DecimalMin("-1.0")
    @DecimalMax("1.0")
    private Double currentValence;

    @NotNull
    @DecimalMin("-1.0")
    @DecimalMax("1.0")
    private Double currentArousal;

    public UUID getSessionId() {
        return sessionId;
    }

    public void setSessionId(UUID sessionId) {
        this.sessionId = sessionId;
    }

    public Double getCurrentValence() {
        return currentValence;
    }

    public void setCurrentValence(Double currentValence) {
        this.currentValence = currentValence;
    }

    public Double getCurrentArousal() {
        return currentArousal;
    }

    public void setCurrentArousal(Double currentArousal) {
        this.currentArousal = currentArousal;
    }
}
