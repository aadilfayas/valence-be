package com.valence.mood;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class NrcVadAnalyzer {

    // Word -> [valence, arousal, dominance]
    private final Map<String, double[]> lexicon = new HashMap<>();

    @PostConstruct
    public void loadLexicon() {
        log.info("Loading NRC-VAD lexicon...");
        // TODO: load from nrc-vad-lexicon.csv
        log.info("NRC-VAD lexicon loaded with {} entries", lexicon.size());
    }
}
