package com.salvacode.orientlink.service;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.theokanning.openai.service.OpenAiService;
import com.fasterxml.jackson.databind.JsonNode;
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
                    "formal": { "zh": "formal Chinese response", "es": "formal Spanish response" },
                    "negotiator": { "zh": "negotiating Chinese response", "es": "negotiating Spanish response" },
                    "direct": { "zh": "direct Chinese response", "es": "direct Spanish response" }
                  }
                }

                For each suggested response (formal, negotiator, direct), provide both the original (zh) and translated (es) versions.

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
    
    /**
     * Generate Chinese response suggestions based on business context.
     * 
     * @param context Business context or provider message
     * @param userIntent What the user wants to communicate
     * @param responseType "formal", "negotiator", "direct", or "all"
     * @return JSON string with suggested responses
     */
    public String generateResponses(String context, String userIntent, String responseType) {
        log.info("Generating {} response(s) for context", responseType);
        
        String userPrompt = String.format("""
                Generate appropriate business responses for this situation in both Chinese (zh) and Spanish (es).

                Context: %s
                User's Intent: %s
                Response Type: %s

                Respond with this exact JSON structure:
                {
                  "responses": {
                    "formal": { "zh": "formal Chinese response (if requested)", "es": "formal Spanish response (if requested)" },
                    "negotiator": { "zh": "negotiating Chinese response (if requested)", "es": "negotiating Spanish response (if requested)" },
                    "direct": { "zh": "direct Chinese response (if requested)", "es": "direct Spanish response (if requested)" }
                  },
                  "explanation": "brief explanation of the approach taken"
                }

                For each response type (formal, negotiator, direct), provide both the original (zh) and translated (es) versions.

                Guidelines:
                - FORMAL: Use 您, 贵公司, respectful terms, complete sentences
                - NEGOTIATOR: Balance politeness with assertiveness, 我们可以, 希望
                - DIRECT: Clear, brief, 我需要, direct questions
                """,
                context,
                userIntent,
                responseType
        );
        
        return callOpenAi(userPrompt);
    }
    
    /**
     * Core method to call OpenAI API with master prompt.
     */
    private String callOpenAi(String userPrompt) {
        try {
            List<ChatMessage> messages = new ArrayList<>();
            messages.add(new ChatMessage(ChatMessageRole.SYSTEM.value(), MASTER_PROMPT));
            messages.add(new ChatMessage(ChatMessageRole.USER.value(), userPrompt));
            
            ChatCompletionRequest request = ChatCompletionRequest.builder()
                    .model(model)
                    .messages(messages)
                    .temperature(0.7)
                    .maxTokens(2000)
                    .build();
            
            String response = openAiService.createChatCompletion(request)
                    .getChoices()
                    .get(0)
                    .getMessage()
                    .getContent();
            
            log.info("OpenAI response received successfully");
            return response;
            
        } catch (Exception e) {
            log.error("Error calling OpenAI API", e);
            throw new RuntimeException("Failed to get response from OpenAI: " + e.getMessage(), e);
        }
    }
    
    /**
     * Utility method to parse JSON response from OpenAI.
     */
    public JsonNode parseJsonResponse(String jsonString) {
        try {
            return objectMapper.readTree(jsonString);
        } catch (Exception e) {
            log.error("Error parsing JSON response from OpenAI", e);
            throw new RuntimeException("Invalid JSON response from OpenAI", e);
        }
    }

     /**
     * Extract provider information from an Alibaba URL or product description.
     * 
     * @param alibabaUrl The Alibaba product/supplier URL
     * @param additionalContext Any additional context about the provider
     * @return JSON string with structured provider data
     */
    public String extractProviderInfo(String alibabaUrl, String additionalContext) {
        log.info("Extracting provider info from URL: {}", alibabaUrl);
        
        String userPrompt = String.format("""
                Analyze this Alibaba provider/product URL and extract business information.
                
                URL: %s
                %s
                
                Note: You cannot actually browse the URL, but infer what data should be extracted.
                Provide a template response showing what information should be collected.
                
                Respond with this exact JSON structure:
                {
                  "providerName": "extracted or 'Unknown'",
                  "productName": "extracted or 'Unknown'",
                  "moq": null or number,
                  "pricePerUnit": null or number,
                  "currency": "USD/CNY/etc or null",
                  "certifications": ["cert1", "cert2"],
                  "deliveryTimeDays": null or number,
                  "additionalInfo": "any other relevant details",
                  "riskAssessment": {
                    "overallRisk": "low/medium/high",
                    "warnings": ["warning 1", "warning 2"],
                    "recommendation": "advice for the buyer"
                  }
                }
                
                Risk assessment should consider:
                - Price too good to be true
                - Very low/high MOQ
                - Lack of certifications
                - Unusual delivery terms
                """,
                alibabaUrl,
                additionalContext != null ? "Additional context: " + additionalContext : ""
        );
        
        return callOpenAi(userPrompt);
    }


}
