package com.valence.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "song_cache")
@Getter
@Setter
public class SongCache {

    @Id
    @Column(name = "spotify_track_id", length = 100)
    private String spotifyTrackId;

    @Column(name = "track_name")
    private String trackName;

    private String artist;

    private Double valence;

    private Double energy;

    @Column(length = 100)
    private String genre;

    @Column(name = "preview_url")
    private String previewUrl;

    @Column(name = "cached_at")
    private LocalDateTime cachedAt = LocalDateTime.now();
}
