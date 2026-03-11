package com.valence.mood;

import com.valence.dto.MoodScoreResponse;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

@Component
@Slf4j
public class NrcVadAnalyzer {

    private static final String[] LEXICON_RESOURCE_PATHS = {
            "classpath:nrc-vad-lexicon.csv",
            "classpath:static/nrc-vad-lexicon.csv"
    };
    private static final Pattern NON_WORD_PATTERN = Pattern.compile("[^a-z\\s]");

    // Word -> [valence, arousal, dominance]
    private final Map<String, double[]> lexicon = new HashMap<>();
    private final ResourceLoader resourceLoader;

    public NrcVadAnalyzer(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @PostConstruct
    public void loadLexicon() {
        Resource resource = resolveLexiconResource();
        log.info("Loading NRC-VAD lexicon from {}", resource);

        CSVFormat csvFormat = CSVFormat.DEFAULT.builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .setTrim(true)
            .get();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8));
             CSVParser parser = new CSVParser(reader, csvFormat)) {

            for (CSVRecord record : parser) {
                String term = record.get("term").trim().toLowerCase(Locale.ROOT);
                if (!StringUtils.hasText(term)) {
                    continue;
                }

                double valence = Double.parseDouble(record.get("valence"));
                double arousal = Double.parseDouble(record.get("arousal"));
                double dominance = Double.parseDouble(record.get("dominance"));
                lexicon.put(term, new double[]{valence, arousal, dominance});
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to load NRC-VAD lexicon", exception);
        }

        log.info("NRC-VAD lexicon loaded with {} entries", lexicon.size());
    }

    public MoodScoreResponse analyzeText(String inputText) {
        if (!StringUtils.hasText(inputText)) {
            return new MoodScoreResponse(0.0, 0.0, "Neutral", List.of());
        }

        String normalizedText = NON_WORD_PATTERN.matcher(inputText.toLowerCase(Locale.ROOT)).replaceAll(" ");
        String[] tokens = normalizedText.trim().split("\\s+");

        double totalValence = 0.0;
        double totalArousal = 0.0;
        int matchedTokenCount = 0;
        Set<String> matchedWords = new LinkedHashSet<>();

        for (String token : tokens) {
            if (!StringUtils.hasText(token)) {
                continue;
            }

            double[] scores = lexicon.get(token);
            if (scores == null) {
                continue;
            }

            totalValence += scores[0];
            totalArousal += scores[1];
            matchedTokenCount++;
            matchedWords.add(token);
        }

        if (matchedTokenCount == 0) {
            return new MoodScoreResponse(0.0, 0.0, "Neutral", List.of());
        }

        double averageValence = totalValence / matchedTokenCount;
        double averageArousal = totalArousal / matchedTokenCount;
        String emotion = mapToEmotion(averageValence, averageArousal);

        return new MoodScoreResponse(
                averageValence,
                averageArousal,
                emotion,
                new ArrayList<>(matchedWords)
        );
    }

    private Resource resolveLexiconResource() {
        for (String path : LEXICON_RESOURCE_PATHS) {
            Resource resource = resourceLoader.getResource(path);
            if (resource.exists()) {
                return resource;
            }
        }
        throw new IllegalStateException("Could not locate nrc-vad-lexicon.csv in classpath");
    }

    private String mapToEmotion(double valence, double arousal) {
        if (valence > 0 && arousal > 0) {
            return "Happy";
        }
        if (valence < 0 && arousal > 0) {
            return "Angry";
        }
        if (valence < 0 && arousal < 0) {
            return "Sad";
        }
        if (valence > 0 && arousal < 0) {
            return "Calm";
        }
        return "Neutral";
    }
}
