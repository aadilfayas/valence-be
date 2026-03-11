package com.valence.recommendation;

import com.valence.repository.MoodSessionRepository;
import com.valence.repository.RecommendationRepository;
import com.valence.repository.SongCacheRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class RecommendService {

    private final RecommendationRepository recommendationRepository;
    private final MoodSessionRepository moodSessionRepository;
    private final SongCacheRepository songCacheRepository;
    private final SpotifyClient spotifyClient;
}
