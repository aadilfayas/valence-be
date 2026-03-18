package com.valence.auth;

import com.valence.dto.UserProfileRequest;
import com.valence.dto.UserProfileResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService userProfileService;

    @GetMapping
    public UserProfileResponse getProfile(Principal principal) {
        return userProfileService.getProfile(principal.getName());
    }

    @PatchMapping
    public UserProfileResponse updateProfile(Principal principal,
                                             @Valid @RequestBody UserProfileRequest request) {
        return userProfileService.updateProfile(principal.getName(), request);
    }
}
