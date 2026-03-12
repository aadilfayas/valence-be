package com.valence.repository;

import com.valence.model.SongCache;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SongCacheRepository extends JpaRepository<SongCache, String> {

		@Query("select s.spotifyTrackId from SongCache s")
		List<String> findAllTrackIds();

		@Query("""
						select s from SongCache s
						where s.valence is not null
							and s.energy is not null
							and lower(s.genre) in :genres
						""")
		List<SongCache> findCandidatesByGenres(@Param("genres") List<String> genres);

		@Query("""
						select s from SongCache s
						where s.valence is not null
							and s.energy is not null
						""")
		List<SongCache> findAllWithMoodMetrics();
}
