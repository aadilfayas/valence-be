package com.valence.mood;

import com.valence.repository.MoodSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class MoodService {

    private final MoodSessionRepository moodSessionRepository;
    private final NrcVadAnalyzer nrcVadAnalyzer;
}
