package com.valence.dto;

import java.util.List;

public class RecommendationGenresResponse {

    private List<String> genres;

    public RecommendationGenresResponse() {
    }

    public RecommendationGenresResponse(List<String> genres) {
        this.genres = genres;
    }

    public List<String> getGenres() {
        return genres;
    }

    public void setGenres(List<String> genres) {
        this.genres = genres;
    }
}
