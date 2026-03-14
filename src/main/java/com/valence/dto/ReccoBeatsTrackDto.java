package com.valence.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReccoBeatsTrackDto {

    private String id;

    @JsonProperty("track_id")
    private String trackId;

    @JsonProperty("trackTitle")
    private String trackTitle;

    private String title;
    private String artist;
    private String href;
    private List<ReccoBeatsArtistDto> artists;

    public String getDisplayTitle() {
        if (trackTitle != null && !trackTitle.isBlank()) {
            return trackTitle;
        }
        return title;
    }

    public String getPrimaryArtistName() {
        if (artists != null && !artists.isEmpty() && artists.get(0).getName() != null) {
            return artists.get(0).getName();
        }
        return artist;
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ReccoBeatsArtistDto {
        private String id;
        private String name;
        private String href;
    }
}