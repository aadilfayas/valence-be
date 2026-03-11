package com.valence.repository;

import com.valence.model.MoodSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface MoodSessionRepository extends JpaRepository<MoodSession, UUID> {

    @Query("SELECT ms FROM MoodSession ms WHERE ms.user.id = :userId ORDER BY ms.createdAt DESC")
    Page<MoodSession> findSessionsByUserId(@Param("userId") UUID userId, Pageable pageable);
}
