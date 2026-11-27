package com.taskmanager.parser;

import com.taskmanager.exception.ServiceUnavailableException;

/** Selects the live parser when configured, or an explicit offline strategy otherwise. */
public final class ParserFactory {
    private ParserFactory() {}

    public static TaskParser create() {
        String key = System.getenv("LLM_API_KEY");
        if (key == null || key.isBlank()) return new RuleBasedTaskParser();
        return new LlmTaskParser(new HttpLlmClient(System.getenv().getOrDefault("LLM_PROVIDER", "openai"), key));
    }

    /** Use this when the caller specifically requires a live LLM (missing key maps to API 503). */
    public static TaskParser createLlmRequired() {
        String key = System.getenv("LLM_API_KEY");
        if (key == null || key.isBlank()) throw new ServiceUnavailableException("LLM_API_KEY is not configured");
        return new LlmTaskParser(new HttpLlmClient(System.getenv().getOrDefault("LLM_PROVIDER", "openai"), key));
    }
}

