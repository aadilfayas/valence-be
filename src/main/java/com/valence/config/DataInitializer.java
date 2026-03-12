package com.valence.config;

import com.valence.model.SongCache;
import com.valence.model.User;
import com.valence.repository.SongCacheRepository;
import com.valence.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements ApplicationRunner {

    private static final String SEED_EMAIL = "admin@valence.dev";
    private static final String SEED_PASSWORD = "changeme123";
    private static final String SEED_DISPLAY_NAME = "Admin";
    private static final String SONG_SEED_CSV = "static/train.csv";
    private static final int SONG_SEED_BATCH_SIZE = 1000;
    private static final int TRACK_NAME_MAX_LENGTH = 255;
    private static final int ARTIST_MAX_LENGTH = 255;
    private static final int GENRE_MAX_LENGTH = 100;

    private final UserRepository userRepository;
    private final SongCacheRepository songCacheRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        log.info("Initial data seeding is disabled. Skipping default user and song_cache CSV seed.");
    }

    private void seedDefaultUser() {
        if (userRepository.existsByEmail(SEED_EMAIL)) {
            log.debug("Default user '{}' already exists - skipping", SEED_EMAIL);
            return;
        }

        User user = new User();
        user.setEmail(SEED_EMAIL);
        user.setPasswordHash(passwordEncoder.encode(SEED_PASSWORD));
        user.setDisplayName(SEED_DISPLAY_NAME);
        userRepository.save(user);
        log.info("Default user created: {}", SEED_EMAIL);
    }

    private void seedSongCacheFromCsv() {
        long existingCount = songCacheRepository.count();
        Set<String> existingTrackIds = new HashSet<>(songCacheRepository.findAllTrackIds());
        if (existingCount > 0) {
            log.info("song_cache currently has {} rows; CSV seed will add only missing track_ids", existingCount);
        }

        ClassPathResource resource = new ClassPathResource(SONG_SEED_CSV);
        if (!resource.exists()) {
            log.warn("Song seed CSV not found at classpath:{}", SONG_SEED_CSV);
            return;
        }

        int inserted = 0;
        int skippedExisting = 0;
        int skippedDuplicates = 0;
        List<SongCache> batch = new ArrayList<>(SONG_SEED_BATCH_SIZE);
        Set<String> seenTrackIds = new HashSet<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8));
             CSVParser parser = CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).get().parse(reader)) {

            for (CSVRecord record : parser) {
                String trackId = record.get("track_id");
                if (trackId == null || trackId.isBlank()) {
                    continue;
                }
                if (existingTrackIds.contains(trackId)) {
                    skippedExisting++;
                    continue;
                }
                if (!seenTrackIds.add(trackId)) {
                    skippedDuplicates++;
                    continue;
                }

                Double valence = parseDouble(record.get("valence"));
                Double energy = parseDouble(record.get("energy"));
                if (valence == null || energy == null) {
                    continue;
                }

                SongCache song = new SongCache();
                song.setSpotifyTrackId(trackId);
                song.setTrackName(trimToMaxLength(record.get("track_name"), TRACK_NAME_MAX_LENGTH));
                song.setArtist(trimToMaxLength(record.get("artists"), ARTIST_MAX_LENGTH));
                song.setGenre(trimToMaxLength(record.get("track_genre"), GENRE_MAX_LENGTH));
                song.setValence(valence);
                song.setEnergy(energy);
                song.setCachedAt(LocalDateTime.now());

                batch.add(song);
                if (batch.size() >= SONG_SEED_BATCH_SIZE) {
                    songCacheRepository.saveAll(batch);
                    inserted += batch.size();
                    batch.clear();
                }
            }

            if (!batch.isEmpty()) {
                songCacheRepository.saveAll(batch);
                inserted += batch.size();
            }

            log.info("Seeded song_cache from {} with {} rows ({} existing rows skipped, {} duplicate track_ids skipped)",
                    SONG_SEED_CSV,
                    inserted,
                    skippedExisting,
                    skippedDuplicates);
        } catch (IOException e) {
            log.error("Failed to seed song_cache from {}", SONG_SEED_CSV, e);
        } catch (Exception e) {
            log.error("Unexpected error while seeding song_cache from {}", SONG_SEED_CSV, e);
        }
    }

    private Double parseDouble(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private String trimToMaxLength(String value, int maxLength) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return null;
        }

        return trimmed.length() > maxLength ? trimmed.substring(0, maxLength) : trimmed;
    }
}
