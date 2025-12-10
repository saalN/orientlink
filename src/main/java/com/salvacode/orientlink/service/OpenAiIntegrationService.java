package com.salvacode.orientlink.service;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.theokanning.openai.service.OpenAiService;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;



/**
 * Service for interacting with OpenAI API.
 * Contains a centralized MASTER PROMPT to ensure consistency across all AI interactions.
 * Handles translation, business analysis, provider extraction, and response generation.
 */
@Service
@Slf4j
public class OpenAiIntegrationService {
    
    private final OpenAiService openAiService;
    private final ObjectMapper objectMapper;
    
    @Value("${openai.model:gpt-4}")
    private String model;
    
    /**
     * MASTER PROMPT - Used as system context for all OpenAI interactions.
     * This ensures consistent behavior and expertise across all features.
     */
    private static final String MASTER_PROMPT = """
            You are an expert AI assistant specializing in Chinese-Spanish business communications,
            with deep knowledge of:
            - Spanish ↔ Chinese translation (Mandarin)
            - Chinese supplier business practices (MOQ, pricing, negotiations)
            - Alibaba and similar B2B platform terminology
            - Risk assessment for international trade
            - Cultural nuances in Chinese business communication
            
            Your capabilities:
            1. Translate accurately between Spanish and Chinese (text only)
            2. Interpret business context: MOQ, pricing, delivery terms, certifications
            3. Identify suspicious terms, unusual requests, or potential risks
            4. Generate appropriate Chinese responses in three tones:
               - FORMAL: Polite, respectful, traditional business style
               - NEGOTIATOR: Friendly but firm, seeking mutual benefit
               - DIRECT: Clear, assertive, time-efficient
            5. Extract structured data from Alibaba product URLs or provider information
            
            Always respond in valid JSON format as specified in each request.
            Be precise, professional, and culturally aware.
            """;

    public OpenAiIntegrationService(@Value("${openai.api-key}") String apiKey) {
      this.openAiService = new OpenAiService(apiKey, Duration.ofSeconds(60));
      this.objectMapper = new ObjectMapper();
    }


}
