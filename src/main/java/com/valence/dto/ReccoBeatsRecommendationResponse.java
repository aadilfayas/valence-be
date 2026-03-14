package com.valence.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.util.Collections;
import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReccoBeatsRecommendationResponse {

    private List<ReccoBeatsTrackDto> content;
    private List<ReccoBeatsTrackDto> recommendations;

    public List<ReccoBeatsTrackDto> getTracks() {
        if (content != null && !content.isEmpty()) {
            return content;
        }
        if (recommendations != null && !recommendations.isEmpty()) {
            return recommendations;
        }
        return Collections.emptyList();
    }
}