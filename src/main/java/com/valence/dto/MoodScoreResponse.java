package com.valence.dto;

import java.util.List;

public class MoodScoreResponse {

    private double valence;
    private double arousal;
    private String emotion;
    private List<String> matchedWords;

    public MoodScoreResponse(double valence, double arousal, String emotion, List<String> matchedWords) {
        this.valence = valence;
        this.arousal = arousal;
        this.emotion = emotion;
        this.matchedWords = matchedWords;
    }

    public double getValence() {
        return valence;
    }

    public void setValence(double valence) {
        this.valence = valence;
    }

    public double getArousal() {
        return arousal;
    }

    public void setArousal(double arousal) {
        this.arousal = arousal;
    }

    public String getEmotion() {
        return emotion;
    }

    public void setEmotion(String emotion) {
        this.emotion = emotion;
    }

    public List<String> getMatchedWords() {
        return matchedWords;
    }

    public void setMatchedWords(List<String> matchedWords) {
        this.matchedWords = matchedWords;
    }
}
