package com.valence.recommendation;

import com.valence.dto.RecommendationListResponse;
import com.valence.dto.RecommendationNextRequest;
import com.valence.dto.RecommendationTrackResponse;
import com.valence.dto.SpotifyTrackDto;
import com.valence.model.MoodSession;
import com.valence.model.Recommendation;
import com.valence.model.SongCache;
import com.valence.repository.MoodSessionRepository;
import com.valence.repository.RecommendationRepository;
import com.valence.repository.SongCacheRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class RecommendService {

    private static final int WAYPOINT_COUNT = 5;
    private static final int SONGS_PER_WAYPOINT = 2;
    private static final int TRACKS_PER_GENRE = 30;
    private static final List<String> DEFAULT_GENRE_POOL = List.of(
            "pop", "rock", "indie", "electronic", "chill", "ambient"
    );

    private final RecommendationRepository recommendationRepository;
    private final MoodSessionRepository moodSessionRepository;
    private final SongCacheRepository songCacheRepository;
    private final SpotifyClient spotifyClient;

    public RecommendationListResponse getRecommendations(UUID sessionId) {
        MoodSession session = getSessionOrThrow(sessionId);
        List<Recommendation> existing = recommendationRepository.findBySessionIdOrderByPositionInPath(sessionId);

        if (!existing.isEmpty()) {
            return new RecommendationListResponse(sessionId, mapRecommendations(existing));
        }

        return generateAndStoreRecommendations(
                session,
                safeDouble(session.getValenceScore()),
                safeDouble(session.getArousalScore()),
                safeDouble(session.getGoalValence()),
                safeDouble(session.getGoalArousal())
        );
    }

    public RecommendationListResponse regenerateFromCurrentPosition(RecommendationNextRequest request) {
        MoodSession session = getSessionOrThrow(request.getSessionId());
        return generateAndStoreRecommendations(
                session,
                safeDouble(request.getCurrentValence()),
                safeDouble(request.getCurrentArousal()),
                safeDouble(session.getGoalValence()),
                safeDouble(session.getGoalArousal())
        );
    }

    private RecommendationListResponse generateAndStoreRecommendations(MoodSession session,
                                                                       double currentValence,
                                                                       double currentArousal,
                                                                       double goalValence,
                                                                       double goalArousal) {
        log.info("Generating recommendation path for session {}", session.getId());

        List<Waypoint> waypoints = generateWaypoints(currentValence, currentArousal, goalValence, goalArousal);
        List<TrackCandidate> candidates = buildTrackCandidatePool(DEFAULT_GENRE_POOL);

        if (candidates.isEmpty()) {
            log.warn("No candidate tracks available for recommendation session {}", session.getId());
            recommendationRepository.deleteBySessionId(session.getId());
            throw new ResponseStatusException(
                    SERVICE_UNAVAILABLE,
                    "Unable to generate recommendations: Spotify Recommendations/Audio Features endpoints are unavailable for this app."
            );
        }

        List<RecommendationTrackResponse> selected = new ArrayList<>();
        Set<String> usedTrackIds = new HashSet<>();

        for (Waypoint waypoint : waypoints) {
            List<ScoredCandidate> scored = candidates.stream()
                    .map(candidate -> scoreCandidate(candidate, waypoint))
                    .sorted(Comparator.comparingDouble(ScoredCandidate::distance))
                    .toList();

            int added = 0;
            for (ScoredCandidate scoredCandidate : scored) {
                if (added >= SONGS_PER_WAYPOINT) {
                    break;
                }
                if (usedTrackIds.contains(scoredCandidate.candidate().spotifyTrackId())) {
                    continue;
                }

                selected.add(toRecommendationTrackResponse(selected.size() + 1, scoredCandidate));
                usedTrackIds.add(scoredCandidate.candidate().spotifyTrackId());
                added++;
            }

            for (ScoredCandidate scoredCandidate : scored) {
                if (added >= SONGS_PER_WAYPOINT) {
                    break;
                }

                selected.add(toRecommendationTrackResponse(selected.size() + 1, scoredCandidate));
                added++;
            }
        }

        recommendationRepository.deleteBySessionId(session.getId());
        List<Recommendation> toPersist = selected.stream()
                .map(response -> toRecommendationEntity(session, response))
                .toList();
        recommendationRepository.saveAll(toPersist);

        log.info("Generated {} recommendations for session {}", selected.size(), session.getId());
        return new RecommendationListResponse(session.getId(), selected);
    }

    private List<TrackCandidate> buildTrackCandidatePool(List<String> genrePool) {
        Map<String, TrackCandidate> candidatesById = new HashMap<>();
        int spotifyTrackCount = 0;

        for (String genre : genrePool) {
            List<SpotifyTrackDto> tracks = spotifyClient.getRecommendations(genre, TRACKS_PER_GENRE);
            spotifyTrackCount += tracks.size();
            for (SpotifyTrackDto track : tracks) {
                if (track == null || track.getId() == null || track.getId().isBlank()) {
                    continue;
                }

                SongCache features = spotifyClient.getTrackFeatures(track, genre);
                if (features.getValence() == null || features.getEnergy() == null) {
                    continue;
                }

                TrackCandidate candidate = new TrackCandidate(
                        track.getId(),
                        track.getName(),
                        track.getFirstArtistName(),
                        safeDouble(features.getValence()),
                        safeDouble(features.getEnergy()),
                        features.getPreviewUrl()
                );
                candidatesById.putIfAbsent(candidate.spotifyTrackId(), candidate);
            }
        }

        if (candidatesById.isEmpty()) {
            List<String> normalizedGenres = genrePool.stream()
                    .map(String::toLowerCase)
                    .collect(Collectors.toList());

            List<SongCache> fallback = songCacheRepository.findCandidatesByGenres(normalizedGenres);
            if (fallback.isEmpty()) {
                fallback = songCacheRepository.findAllWithMoodMetrics();
            }

            for (SongCache song : fallback) {
                if (song.getSpotifyTrackId() == null || song.getSpotifyTrackId().isBlank()) {
                    continue;
                }

                TrackCandidate candidate = new TrackCandidate(
                        song.getSpotifyTrackId(),
                        song.getTrackName(),
                        song.getArtist(),
                        safeDouble(song.getValence()),
                        safeDouble(song.getEnergy()),
                        song.getPreviewUrl()
                );
                candidatesById.putIfAbsent(candidate.spotifyTrackId(), candidate);
            }

            log.warn("Spotify candidate fetch returned no tracks ({} requested). Using local song_cache fallback with {} tracks",
                    spotifyTrackCount,
                    candidatesById.size());
        }

        List<TrackCandidate> candidates = new ArrayList<>(candidatesById.values());
        log.info("Built candidate pool with {} tracks", candidates.size());
        return candidates;
    }

    private List<Waypoint> generateWaypoints(double currentValence,
                                             double currentArousal,
                                             double goalValence,
                                             double goalArousal) {
        List<Waypoint> waypoints = new ArrayList<>(WAYPOINT_COUNT);
        for (int i = 0; i < WAYPOINT_COUNT; i++) {
            double ratio = (double) i / (WAYPOINT_COUNT - 1);
            double valence = currentValence + ratio * (goalValence - currentValence);
            double arousal = currentArousal + ratio * (goalArousal - currentArousal);
            waypoints.add(new Waypoint(valence, arousal));
        }
        return waypoints;
    }

    private ScoredCandidate scoreCandidate(TrackCandidate candidate, Waypoint waypoint) {
        double distance = euclideanDistance(
                candidate.valence(),
                candidate.energy(),
                waypoint.valence(),
                waypoint.arousal()
        );
        return new ScoredCandidate(candidate, distance);
    }

    private double euclideanDistance(double v1, double a1, double v2, double a2) {
        double dv = v1 - v2;
        double da = a1 - a2;
        return Math.sqrt((dv * dv) + (da * da));
    }

    private RecommendationTrackResponse toRecommendationTrackResponse(int position, ScoredCandidate scored) {
        TrackCandidate candidate = scored.candidate();
        return new RecommendationTrackResponse(
                position,
                candidate.spotifyTrackId(),
                candidate.trackName(),
                candidate.artist(),
                candidate.valence(),
                candidate.energy(),
                scored.distance(),
                candidate.previewUrl()
        );
    }

    private Recommendation toRecommendationEntity(MoodSession session, RecommendationTrackResponse response) {
        Recommendation entity = new Recommendation();
        entity.setSession(session);
        entity.setPositionInPath(response.getPositionInPath());
        entity.setSpotifyTrackId(response.getSpotifyTrackId());
        entity.setTrackName(response.getTrackName());
        entity.setArtist(response.getArtist());
        entity.setValence(response.getValence());
        entity.setEnergy(response.getEnergy());
        return entity;
    }

    private List<RecommendationTrackResponse> mapRecommendations(List<Recommendation> recommendations) {
        return recommendations.stream()
                .map(recommendation -> {
                    String previewUrl = songCacheRepository.findById(recommendation.getSpotifyTrackId())
                            .map(SongCache::getPreviewUrl)
                            .orElse(null);
                    return new RecommendationTrackResponse(
                            recommendation.getPositionInPath(),
                            recommendation.getSpotifyTrackId(),
                            recommendation.getTrackName(),
                            recommendation.getArtist(),
                            safeDouble(recommendation.getValence()),
                            safeDouble(recommendation.getEnergy()),
                            0.0,
                            previewUrl
                    );
                })
                .toList();
    }

    private MoodSession getSessionOrThrow(UUID sessionId) {
        return moodSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Mood session not found"));
    }

    private double safeDouble(Double value) {
        return value == null ? 0.0 : value;
    }

    private record TrackCandidate(String spotifyTrackId, String trackName, String artist, double valence, double energy, String previewUrl) {
    }

    private record Waypoint(double valence, double arousal) {
    }

    private record ScoredCandidate(TrackCandidate candidate, double distance) {
    }
}
