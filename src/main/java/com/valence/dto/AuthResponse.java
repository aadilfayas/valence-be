package com.valence.dto;

import java.util.UUID;

public class AuthResponse {

    private UUID id;
    private String token;
    private String email;
    private String displayName;

    public AuthResponse(UUID id, String token, String email, String displayName) {
        this.id = id;
        this.token = token;
        this.email = email;
        this.displayName = displayName;
    }

    public UUID getId() { return id; }
    public String getToken() { return token; }
    public String getEmail() { return email; }
    public String getDisplayName() { return displayName; }
}
