package com.rxvaani.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ExtractRequest {
    private String transcript;

    @JsonProperty("llm_provider")
    private String llmProvider;

    @JsonProperty("api_key")
    private String apiKey;

    public String getTranscript() { return transcript; }
    public void setTranscript(String transcript) { this.transcript = transcript; }

    public String getLlmProvider() { return llmProvider; }
    public void setLlmProvider(String llmProvider) { this.llmProvider = llmProvider; }

    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }
}
