package com.valence.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "display_name", length = 100)
    private String displayName;

    /**
     * Comma-separated preferred genre list, e.g. "pop,indie,chill".
     * Maximum 8 genres. Stored as VARCHAR so no extra join table is needed.
     * Parsed/serialised by UserProfileService.
     */
    @Column(name = "preferred_genres", length = 500)
    private String preferredGenres;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
