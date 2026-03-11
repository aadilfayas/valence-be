package com.valence.mood;

import com.valence.dto.MoodAnalyzeRequest;
import com.valence.dto.MoodScoreResponse;
import com.valence.dto.MoodSessionCreateRequest;
import com.valence.dto.MoodSessionHistoryResponse;
import com.valence.model.MoodSession;
import com.valence.model.User;
import com.valence.repository.MoodSessionRepository;
import com.valence.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class MoodService {

    private final MoodSessionRepository moodSessionRepository;
    private final UserRepository userRepository;
    private final NrcVadAnalyzer nrcVadAnalyzer;

    @Transactional(readOnly = true)
    public MoodScoreResponse analyzeMood(MoodAnalyzeRequest request) {
        log.info("Analyzing mood text ({} chars)", request.getMessage().length());
        return nrcVadAnalyzer.analyzeText(request.getMessage());
    }

    @Transactional(readOnly = true)
    public List<String> getChatQuestions() {
        return List.of(
                "How are you feeling?",
                "What triggered it?",
                "What mood do you want to reach?"
        );
    }

    public UUID createMoodSession(MoodSessionCreateRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User not found"));

        MoodScoreResponse feelingScore = nrcVadAnalyzer.analyzeText(request.getFeelingMessage());
        MoodScoreResponse triggerScore = nrcVadAnalyzer.analyzeText(request.getTriggerMessage());
        MoodScoreResponse goalScore = nrcVadAnalyzer.analyzeText(request.getGoalMessage());

        double currentValence = (feelingScore.getValence() + triggerScore.getValence()) / 2.0;
        double currentArousal = (feelingScore.getArousal() + triggerScore.getArousal()) / 2.0;

        MoodSession session = new MoodSession();
        session.setUser(user);
        session.setRawText(buildRawConversation(request));
        session.setValenceScore(currentValence);
        session.setArousalScore(currentArousal);
        session.setGoalValence(goalScore.getValence());
        session.setGoalArousal(goalScore.getArousal());

        MoodSession savedSession = moodSessionRepository.save(session);
        log.info("Created mood session {} for user {}", savedSession.getId(), request.getUserId());
        return savedSession.getId();
    }

    @Transactional(readOnly = true)
    public Page<MoodSessionHistoryResponse> getMoodSessions(UUID userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return moodSessionRepository.findSessionsByUserId(userId, pageable)
                .map(this::toHistoryResponse);
    }

    private MoodSessionHistoryResponse toHistoryResponse(MoodSession session) {
        return new MoodSessionHistoryResponse(
                session.getId(),
                safeDouble(session.getValenceScore()),
                safeDouble(session.getArousalScore()),
                safeDouble(session.getGoalValence()),
                safeDouble(session.getGoalArousal()),
                session.getCreatedAt()
        );
    }

    private String buildRawConversation(MoodSessionCreateRequest request) {
        return "Q1: How are you feeling?\n"
                + "A1: " + request.getFeelingMessage() + "\n"
                + "Q2: What triggered it?\n"
                + "A2: " + request.getTriggerMessage() + "\n"
                + "Q3: What mood do you want to reach?\n"
                + "A3: " + request.getGoalMessage();
    }

    private double safeDouble(Double value) {
        return value == null ? 0.0 : value;
    }
}
