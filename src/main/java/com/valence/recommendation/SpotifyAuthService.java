package com.valence.recommendation;

import com.valence.dto.SpotifyTokenResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;

@Service
@Slf4j
public class SpotifyAuthService {

    private static final String TOKEN_URL = "https://accounts.spotify.com/api/token";
    private static final int REFRESH_BUFFER_SECONDS = 60;

    @Value("${spotify.client-id}")
    private String clientId;

    @Value("${spotify.client-secret}")
    private String clientSecret;

    private final RestTemplate restTemplate = new RestTemplate();

    private String cachedToken;
    private Instant tokenExpiryTime = Instant.EPOCH;

    /**
     * Returns a valid Spotify access token, refreshing it if it is expired
     * or within REFRESH_BUFFER_SECONDS of expiry.
     */
    public synchronized String getValidToken() {
        if (Instant.now().isAfter(tokenExpiryTime.minusSeconds(REFRESH_BUFFER_SECONDS))) {
            refreshToken();
        }
        return cachedToken;
    }

    private void refreshToken() {
        log.info("Refreshing Spotify access token");

        String credentials = clientId + ":" + clientSecret;
        String encodedCredentials = Base64.getEncoder()
                .encodeToString(credentials.getBytes(StandardCharsets.UTF_8));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set(HttpHeaders.AUTHORIZATION, "Basic " + encodedCredentials);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "client_credentials");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        ResponseEntity<SpotifyTokenResponse> response =
                restTemplate.postForEntity(TOKEN_URL, request, SpotifyTokenResponse.class);

        SpotifyTokenResponse tokenResponse = response.getBody();
        if (tokenResponse == null || tokenResponse.getAccessToken() == null) {
            throw new IllegalStateException("Failed to obtain Spotify access token");
        }

        this.cachedToken = tokenResponse.getAccessToken();
        this.tokenExpiryTime = Instant.now().plusSeconds(tokenResponse.getExpiresIn());
        log.info("Spotify token refreshed, expires in {}s", tokenResponse.getExpiresIn());
    }
}
