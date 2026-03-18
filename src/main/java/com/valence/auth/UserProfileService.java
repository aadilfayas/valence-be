package com.valence.auth;

import com.valence.dto.UserProfileRequest;
import com.valence.dto.UserProfileResponse;
import com.valence.model.User;
import com.valence.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UserProfileService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(String email) {
        User user = findUserByEmail(email);
        return toResponse(user);
    }

    public UserProfileResponse updateProfile(String email, UserProfileRequest request) {
        User user = findUserByEmail(email);

        if (request.getDisplayName() != null && !request.getDisplayName().isBlank()) {
            user.setDisplayName(request.getDisplayName().trim());
        }

        if (request.getPreferredGenres() != null) {
            String serialized = request.getPreferredGenres().stream()
                    .filter(g -> g != null && !g.isBlank())
                    .map(String::trim)
                    .map(String::toLowerCase)
                    .distinct()
                    .limit(8)
                    .collect(Collectors.joining(","));
            user.setPreferredGenres(serialized.isEmpty() ? null : serialized);
        }

        userRepository.save(user);
        log.info("Updated profile for user: {}", email);
        return toResponse(user);
    }

    /**
     * Parses the comma-separated genre string stored on the User entity
     * into a clean list. Safe to call with null. Public so RecommendService
     * can reuse without a cross-package dependency.
     */
    public static List<String> parseGenres(String serialized) {
        if (serialized == null || serialized.isBlank()) {
            return Collections.emptyList();
        }
        return Arrays.stream(serialized.split(","))
                .map(String::trim)
                .filter(g -> !g.isEmpty())
                .toList();
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private UserProfileResponse toResponse(User user) {
        return new UserProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getDisplayName(),
                parseGenres(user.getPreferredGenres())
        );
    }
}
