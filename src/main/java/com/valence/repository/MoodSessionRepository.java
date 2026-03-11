package com.valence.repository;

import com.valence.model.MoodSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MoodSessionRepository extends JpaRepository<MoodSession, UUID> {
    Page<MoodSession> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);
}
