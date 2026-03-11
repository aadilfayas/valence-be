package com.valence.repository;

import com.valence.model.Recommendation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RecommendationRepository extends JpaRepository<Recommendation, UUID> {
    List<Recommendation> findBySessionIdOrderByPositionInPath(UUID sessionId);
    void deleteBySessionId(UUID sessionId);
}
