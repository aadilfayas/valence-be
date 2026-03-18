package com.valence.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class UserProfileResponse {

    private UUID id;
    private String email;
    private String displayName;
    private List<String> preferredGenres;
}
