package com.valence.recommendation;

import com.valence.dto.ReccoBeatsRecommendationResponse;
import com.valence.dto.ReccoBeatsTrackDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
@Slf4j
public class ReccoBeatsClient {

    private static final Pattern SPOTIFY_TRACK_URL_PATTERN = Pattern.compile("open\\.spotify\\.com/track/([A-Za-z0-9]{22})");
    private static final Pattern SPOTIFY_TRACK_URI_PATTERN = Pattern.compile("spotify:track:([A-Za-z0-9]{22})");

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${reccobeats.base-url:https://api.reccobeats.com}")
    private String baseUrl;

    public List<ReccoBeatsTrackDto> getRecommendations(List<String> seeds, int size) {
        List<String> normalizedSeeds = seeds == null
                ? Collections.emptyList()
                : seeds.stream().filter(this::hasText).distinct().toList();

        if (normalizedSeeds.isEmpty()) {
            return Collections.emptyList();
        }

        String url = UriComponentsBuilder.fromUriString(baseUrl + "/v1/track/recommendation")
                .queryParam("seeds", normalizedSeeds.stream().collect(Collectors.joining(",")))
                .queryParam("size", Math.max(size, 1))
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<ReccoBeatsRecommendationResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    request,
                    ReccoBeatsRecommendationResponse.class
            );

            ReccoBeatsRecommendationResponse body = response.getBody();
            if (body == null) {
                log.warn("Received empty body from ReccoBeats recommendation API");
                return Collections.emptyList();
            }

            List<ReccoBeatsTrackDto> tracks = body.getTracks();
            log.info("Fetched {} ReccoBeats recommendations for {} seeds", tracks.size(), normalizedSeeds.size());
            return tracks;
        } catch (HttpStatusCodeException e) {
            log.error("Failed to fetch ReccoBeats recommendations: status={} body={}",
                    e.getStatusCode().value(),
                    trimResponseBody(e.getResponseBodyAsString()));
            return Collections.emptyList();
        } catch (Exception e) {
            log.error("Failed to fetch ReccoBeats recommendations: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    public String extractSpotifyTrackId(ReccoBeatsTrackDto track) {
        if (track == null) {
            return null;
        }

        if (isSpotifyTrackId(track.getId())) {
            return track.getId();
        }
        if (isSpotifyTrackId(track.getTrackId())) {
            return track.getTrackId();
        }

        String href = track.getHref();
        if (!hasText(href)) {
            return null;
        }

        Matcher urlMatcher = SPOTIFY_TRACK_URL_PATTERN.matcher(href);
        if (urlMatcher.find()) {
            return urlMatcher.group(1);
        }

        Matcher uriMatcher = SPOTIFY_TRACK_URI_PATTERN.matcher(href);
        if (uriMatcher.find()) {
            return uriMatcher.group(1);
        }

        return null;
    }

    private boolean isSpotifyTrackId(String value) {
        return hasText(value) && value.matches("[A-Za-z0-9]{22}");
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String trimResponseBody(String body) {
        if (!hasText(body)) {
            return "<empty>";
        }

        String normalized = body.replaceAll("\\s+", " ").trim();
        return normalized.length() > 300 ? normalized.substring(0, 300) + "..." : normalized;
    }
}