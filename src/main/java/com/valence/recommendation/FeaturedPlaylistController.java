package com.valence.recommendation;

import com.valence.dto.FeaturedPlaylistResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/spotify")
public class FeaturedPlaylistController {

    private final String featuredPlaylistId;

    public FeaturedPlaylistController(
            @Value("${spotify.featured-playlist-id:}") String featuredPlaylistId) {
        this.featuredPlaylistId = featuredPlaylistId;
    }

    @GetMapping("/featured-playlist")
    public FeaturedPlaylistResponse getFeaturedPlaylist() {
        return new FeaturedPlaylistResponse(featuredPlaylistId);
    }
}
