package com.valence.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "recommendations")
@Getter
@Setter
public class Recommendation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id")
    private MoodSession session;

    @Column(name = "spotify_track_id", length = 100)
    private String spotifyTrackId;

    @Column(name = "track_name")
    private String trackName;

    private String artist;

    private Double valence;

    private Double energy;

    @Column(name = "position_in_path")
    private Integer positionInPath;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
