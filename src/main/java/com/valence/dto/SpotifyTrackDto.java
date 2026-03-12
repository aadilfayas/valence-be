package com.valence.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class SpotifyTrackDto {

    private String id;
    private String name;
    private List<SpotifyArtistDto> artists;

    @JsonProperty("preview_url")
    private String previewUrl;

    public String getFirstArtistName() {
        if (artists != null && !artists.isEmpty()) {
            return artists.get(0).getName();
        }
        return null;
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SpotifyArtistDto {
        private String name;
    }
}
