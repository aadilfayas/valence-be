package com.valence.recommendation;

import com.valence.dto.SpotifyAudioFeaturesResponse;
import com.valence.dto.SpotifyRecommendationsResponse;
import com.valence.dto.SpotifyTrackDto;
import com.valence.model.SongCache;
import com.valence.repository.SongCacheRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class SpotifyClient {

    private static final String API_BASE = "https://api.spotify.com/v1";
    private static final int CACHE_TTL_DAYS = 7;

    private final SpotifyAuthService spotifyAuthService;
    private final SongCacheRepository songCacheRepository;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Returns audio features (valence, energy) for a single track.
     * Checks song_cache first; on miss fetches from Spotify and stores the result.
     * Cache entries are considered valid for {@value #CACHE_TTL_DAYS} days.
     */
    @Transactional
    public SongCache getTrackFeatures(String trackId) {
        return getTrackFeatures(trackId, null, null, null);
    }

    /**
     * Returns audio features for a track while enriching cache rows with metadata when available.
     * If a valid cache row exists (within TTL), it is returned without calling Spotify.
     */
    @Transactional
    public SongCache getTrackFeatures(SpotifyTrackDto track, String genre) {
        if (track == null || track.getId() == null || track.getId().isBlank()) {
            throw new IllegalArgumentException("Track id is required");
        }
        return getTrackFeatures(track.getId(), track.getName(), track.getFirstArtistName(), genre);
    }

    private SongCache getTrackFeatures(String trackId, String trackName, String artist, String genre) {
        Optional<SongCache> cached = songCacheRepository.findById(trackId);
        if (cached.isPresent() && isWithinCacheTtl(cached.get().getCachedAt())) {
            log.debug("Cache hit for track {}", trackId);
            SongCache cachedEntry = cached.get();
            boolean updated = false;

            if (hasText(trackName) && !hasText(cachedEntry.getTrackName())) {
                cachedEntry.setTrackName(trackName);
                updated = true;
            }
            if (hasText(artist) && !hasText(cachedEntry.getArtist())) {
                cachedEntry.setArtist(artist);
                updated = true;
            }
            if (hasText(genre) && !hasText(cachedEntry.getGenre())) {
                cachedEntry.setGenre(genre);
                updated = true;
            }

            return updated ? songCacheRepository.save(cachedEntry) : cachedEntry;
        }

        log.debug("Cache miss for track {}, fetching audio features from Spotify", trackId);
        SpotifyAudioFeaturesResponse features = fetchAudioFeatures(trackId);

        SongCache entry = cached.orElse(new SongCache());
        entry.setSpotifyTrackId(features.getId());
        entry.setTrackName(trackName);
        entry.setArtist(artist);
        entry.setGenre(genre);
        entry.setValence(features.getValence());
        entry.setEnergy(features.getEnergy());
        entry.setCachedAt(LocalDateTime.now());
        return songCacheRepository.save(entry);
    }

    /**
     * Fetches track recommendations for a seed genre from Spotify's /recommendations endpoint.
     * Returns a list of track DTOs containing id, name, and first artist.
     */
    public List<SpotifyTrackDto> getRecommendations(String genre, int limit) {
        String token = spotifyAuthService.getValidToken();

        String url = UriComponentsBuilder.fromUriString(API_BASE + "/recommendations")
                .queryParam("seed_genres", genre)
                .queryParam("limit", limit)
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<SpotifyRecommendationsResponse> response = restTemplate.exchange(
                    url, HttpMethod.GET, request, SpotifyRecommendationsResponse.class);

            SpotifyRecommendationsResponse body = response.getBody();
            if (body == null || body.getTracks() == null) {
                log.warn("Empty recommendations response for genre '{}'", genre);
                return Collections.emptyList();
            }

            log.info("Fetched {} recommendations for genre '{}'", body.getTracks().size(), genre);
            return body.getTracks();
        } catch (Exception e) {
            log.error("Failed to fetch recommendations for genre '{}': {}", genre, e.getMessage());
            return Collections.emptyList();
        }
    }

    private SpotifyAudioFeaturesResponse fetchAudioFeatures(String trackId) {
        String token = spotifyAuthService.getValidToken();
        String url = API_BASE + "/audio-features/" + trackId;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<SpotifyAudioFeaturesResponse> response = restTemplate.exchange(
                url, HttpMethod.GET, request, SpotifyAudioFeaturesResponse.class);

        SpotifyAudioFeaturesResponse body = response.getBody();
        if (body == null) {
            throw new IllegalStateException("Null audio features response for track " + trackId);
        }
        return body;
    }

    private boolean isWithinCacheTtl(LocalDateTime cachedAt) {
        return cachedAt != null && cachedAt.isAfter(LocalDateTime.now().minusDays(CACHE_TTL_DAYS));
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
