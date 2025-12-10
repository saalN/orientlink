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

     /**
     * Analyze a user message: translate, interpret business context, and generate alerts.
     * 
     * @param messageText The original message from user or provider
     * @param sourceLang Source language ("es" or "zh")
     * @param targetLang Target language ("es" or "zh")
     * @param conversationContext Optional previous conversation summary
     * @return JSON string with translation, interpretation, alerts, and suggestions
     */
    public String analyzeMessage(String messageText, String sourceLang, String targetLang, 
                                  String conversationContext) {
        log.info("Analyzing message from {} to {}", sourceLang, targetLang);
        
        String userPrompt = String.format("""
                Analyze this business message and provide a comprehensive response in JSON format.
                
                Message: "%s"
                Source Language: %s
                Target Language: %s
                %s
                
                Respond with this exact JSON structure:
                {
                  "translatedMessage": "accurate translation here",
                  "interpretation": {
                    "businessContext": "explain what this message means in business terms",
                    "sentiment": "positive/neutral/negative/urgent",
                    "keyTerms": ["list", "of", "important", "business", "terms"],
                    "riskLevel": "low/medium/high"
                  },
                  "alerts": ["warning 1", "warning 2"],
                  "suggestedResponses": {
                    "formal": "formal Chinese response",
                    "negotiator": "negotiating Chinese response",
                    "direct": "direct Chinese response"
                  }
                }
                
                Alerts should include:
                - Unusual MOQ requirements
                - Suspicious pricing
                - Unclear delivery terms
                - Missing certifications mentions
                - Pressure tactics or urgency without justification
                """,
                messageText,
                sourceLang,
                targetLang,
                conversationContext != null ? "Previous context: " + conversationContext : ""
        );
        
        return callOpenAi(userPrompt);
    }


}
