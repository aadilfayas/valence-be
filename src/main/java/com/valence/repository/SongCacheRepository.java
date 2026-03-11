package com.valence.repository;

import com.valence.model.SongCache;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SongCacheRepository extends JpaRepository<SongCache, String> {
}
