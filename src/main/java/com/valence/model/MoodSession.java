package com.valence.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "mood_sessions")
@Getter
@Setter
public class MoodSession {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "raw_text", columnDefinition = "TEXT")
    private String rawText;

    @Column(name = "valence_score")
    private Double valenceScore;

    @Column(name = "arousal_score")
    private Double arousalScore;

    @Column(name = "goal_valence")
    private Double goalValence;

    @Column(name = "goal_arousal")
    private Double goalArousal;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
