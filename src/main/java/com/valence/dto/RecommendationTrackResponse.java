package com.valence.dto;

public class RecommendationTrackResponse {

    private int positionInPath;
    private String spotifyTrackId;
    private String trackName;
    private String artist;
    private double valence;
    private double energy;
    private double distanceToWaypoint;
    private String previewUrl;

    public RecommendationTrackResponse(int positionInPath,
                                       String spotifyTrackId,
                                       String trackName,
                                       String artist,
                                       double valence,
                                       double energy,
                                       double distanceToWaypoint) {
        this(positionInPath, spotifyTrackId, trackName, artist, valence, energy, distanceToWaypoint, null);
    }

    public RecommendationTrackResponse(int positionInPath,
                                       String spotifyTrackId,
                                       String trackName,
                                       String artist,
                                       double valence,
                                       double energy,
                                       double distanceToWaypoint,
                                       String previewUrl) {
        this.positionInPath = positionInPath;
        this.spotifyTrackId = spotifyTrackId;
        this.trackName = trackName;
        this.artist = artist;
        this.valence = valence;
        this.energy = energy;
        this.distanceToWaypoint = distanceToWaypoint;
        this.previewUrl = previewUrl;
    }

    public int getPositionInPath() {
        return positionInPath;
    }

    public void setPositionInPath(int positionInPath) {
        this.positionInPath = positionInPath;
    }

    public String getSpotifyTrackId() {
        return spotifyTrackId;
    }

    public void setSpotifyTrackId(String spotifyTrackId) {
        this.spotifyTrackId = spotifyTrackId;
    }

    public String getTrackName() {
        return trackName;
    }

    public void setTrackName(String trackName) {
        this.trackName = trackName;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public double getValence() {
        return valence;
    }

    public void setValence(double valence) {
        this.valence = valence;
    }

    public double getEnergy() {
        return energy;
    }

    public void setEnergy(double energy) {
        this.energy = energy;
    }

    public double getDistanceToWaypoint() {
        return distanceToWaypoint;
    }

    public void setDistanceToWaypoint(double distanceToWaypoint) {
        this.distanceToWaypoint = distanceToWaypoint;
    }

    public String getPreviewUrl() {
        return previewUrl;
    }

    public void setPreviewUrl(String previewUrl) {
        this.previewUrl = previewUrl;
    }
}
