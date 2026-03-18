package com.valence.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UserProfileRequest {

    @Size(max = 100)
    private String displayName;

    @Size(max = 8, message = "Maximum 8 preferred genres allowed")
    private List<String> preferredGenres;
}
