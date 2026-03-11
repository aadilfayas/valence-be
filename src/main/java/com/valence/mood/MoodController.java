package com.valence.mood;

import com.valence.dto.MoodAnalyzeRequest;
import com.valence.dto.MoodScoreResponse;
import com.valence.dto.MoodSessionCreateRequest;
import com.valence.dto.MoodSessionCreateResponse;
import com.valence.dto.MoodSessionHistoryResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/mood")
@RequiredArgsConstructor
public class MoodController {

    private final MoodService moodService;

    @GetMapping("/chat/questions")
    public List<String> getChatQuestions() {
        return moodService.getChatQuestions();
    }

    @PostMapping("/analyze")
    public MoodScoreResponse analyzeMood(@Valid @RequestBody MoodAnalyzeRequest request) {
        return moodService.analyzeMood(request);
    }

    @PostMapping("/session")
    public MoodSessionCreateResponse createMoodSession(@Valid @RequestBody MoodSessionCreateRequest request) {
        UUID sessionId = moodService.createMoodSession(request);
        return new MoodSessionCreateResponse(sessionId);
    }

    @GetMapping("/sessions")
    public Page<MoodSessionHistoryResponse> getMoodSessions(
            @RequestParam UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return moodService.getMoodSessions(userId, page, size);
    }
}
