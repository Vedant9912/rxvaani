package com.rxvaani.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TranscribeRequest {
    @JsonProperty("audio_base64")
    private String audioBase64;

    @JsonProperty("scenario_id")
    private String scenarioId;

    private String filename = "consultation.wav";

    @JsonProperty("gnani_key")
    private String gnaniKey;

    public String getAudioBase64() { return audioBase64; }
    public void setAudioBase64(String audioBase64) { this.audioBase64 = audioBase64; }

    public String getScenarioId() { return scenarioId; }
    public void setScenarioId(String scenarioId) { this.scenarioId = scenarioId; }

    public String getFilename() { return filename; }
    public void setFilename(String filename) { this.filename = filename; }

    public String getGnaniKey() { return gnaniKey; }
    public void setGnaniKey(String gnaniKey) { this.gnaniKey = gnaniKey; }
}
