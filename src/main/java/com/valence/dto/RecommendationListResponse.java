package com.valence.dto;

import java.util.List;
import java.util.UUID;

public class RecommendationListResponse {

    private UUID sessionId;
    private List<RecommendationTrackResponse> tracks;

    public RecommendationListResponse(UUID sessionId, List<RecommendationTrackResponse> tracks) {
        this.sessionId = sessionId;
        this.tracks = tracks;
    }

    public UUID getSessionId() {
        return sessionId;
    }

    public void setSessionId(UUID sessionId) {
        this.sessionId = sessionId;
    }

    public List<RecommendationTrackResponse> getTracks() {
        return tracks;
    }

    public void setTracks(List<RecommendationTrackResponse> tracks) {
        this.tracks = tracks;
    }
}
