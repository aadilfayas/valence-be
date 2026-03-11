package com.valence.recommendation;

import com.valence.dto.RecommendationListResponse;
import com.valence.dto.RecommendationNextRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
public class RecommendController {

    private final RecommendService recommendService;

    @GetMapping("/{sessionId}")
    public RecommendationListResponse getRecommendations(@PathVariable UUID sessionId) {
        return recommendService.getRecommendations(sessionId);
    }

    @PostMapping("/next")
    public RecommendationListResponse getNextRecommendations(@Valid @RequestBody RecommendationNextRequest request) {
        return recommendService.regenerateFromCurrentPosition(request);
    }
}
