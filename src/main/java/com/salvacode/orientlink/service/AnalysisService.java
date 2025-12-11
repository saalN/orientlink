package com.salvacode.orientlink.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.salvacode.orientlink.dto.AnalyzeRequestDTO;
import com.salvacode.orientlink.dto.AnalyzeResponseDTO;
import com.salvacode.orientlink.entity.ConversationHistory;
import com.salvacode.orientlink.entity.ProviderProfile;
import com.salvacode.orientlink.repository.ConversationHistoryRepository;
import com.salvacode.orientlink.repository.ProviderProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for analyzing user messages about provider conversations.
 * Orchestrates OpenAI calls and database persistence.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AnalysisService {
    
    private final OpenAiIntegrationService openAiService;
    private final ConversationHistoryRepository conversationRepository;
    private final ProviderProfileRepository providerRepository;
    
    /**
     * Analyze a message: translate, interpret, alert, and suggest responses.
     * Saves the conversation to database.
     */
    @Transactional
    public AnalyzeResponseDTO analyzeMessage(AnalyzeRequestDTO request) {
        log.info("Analyzing message for user: {}", request.getUserId());
        
        // Auto-detect or default languages
        String sourceLang = request.getSourceLanguage() != null ? request.getSourceLanguage() : "es";
        String targetLang = request.getTargetLanguage() != null ? request.getTargetLanguage() : "zh";
        
        // Get provider context if provided
        ProviderProfile provider = null;
        if (request.getProviderId() != null) {
            provider = providerRepository.findById(request.getProviderId()).orElse(null);
        }
        
        // Call OpenAI for analysis
        String aiResponse = openAiService.analyzeMessage(
                request.getMessageText(),
                sourceLang,
                targetLang,
                request.getConversationContext()
        );
        
        // Parse JSON response
        JsonNode jsonResponse = openAiService.parseJsonResponse(aiResponse);
        
        // Build response DTO
        AnalyzeResponseDTO response = buildAnalyzeResponse(jsonResponse, request.getMessageText(), 
                sourceLang, targetLang);
        
        // Save conversation to database
        ConversationHistory conversation = saveConversation(
                request, provider, response, aiResponse
        );
        response.setConversationId(conversation.getId());
        
        log.info("Analysis completed and saved with ID: {}", conversation.getId());
        return response;
    }
    
    /**
     * Build AnalyzeResponse from OpenAI JSON response.
     */
    private AnalyzeResponseDTO buildAnalyzeResponse(JsonNode json, String originalMessage,
                                                  String sourceLang, String targetLang) {
        // Extract interpretation
        JsonNode interpretation = json.get("interpretation");
        AnalyzeResponseDTO.InterpretationData interpretationData = AnalyzeResponseDTO.InterpretationData.builder()
                .businessContext(interpretation.get("businessContext").asText())
                .sentiment(interpretation.get("sentiment").asText())
                .keyTerms(extractListFromJson(interpretation.get("keyTerms")))
                .riskLevel(interpretation.get("riskLevel").asText())
                .build();
        
        // Extract alerts
        List<String> alerts = extractListFromJson(json.get("alerts"));
        
        // Extract suggested responses
        JsonNode responses = json.get("suggestedResponses");
        AnalyzeResponseDTO.SuggestedResponses suggestedResponses = AnalyzeResponseDTO.SuggestedResponses.builder()
                .formal(responses.get("formal").asText())
                .negotiator(responses.get("negotiator").asText())
                .direct(responses.get("direct").asText())
                .build();
        
        return AnalyzeResponseDTO.builder()
                .originalMessage(originalMessage)
                .translatedMessage(json.get("translatedMessage").asText())
                .sourceLanguage(sourceLang)
                .targetLanguage(targetLang)
                .interpretation(interpretationData)
                .alerts(alerts)
                .suggestedResponses(suggestedResponses)
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    /**
     * Save conversation to database.
     */
    private ConversationHistory saveConversation(AnalyzeRequestDTO request, ProviderProfile provider,
                                                  AnalyzeResponseDTO response, String rawAiResponse) {
        ConversationHistory conversation = ConversationHistory.builder()
                .userId(request.getUserId())
                .provider(provider)
                .originalMessage(request.getMessageText())
                .translatedMessage(response.getTranslatedMessage())
                .sourceLanguage(response.getSourceLanguage())
                .targetLanguage(response.getTargetLanguage())
                .aiInterpretation(response.getInterpretation().getBusinessContext())
                .alerts(String.join("; ", response.getAlerts()))
                .suggestedResponses(rawAiResponse) // Store full JSON for reference
                .messageType("analysis")
                .build();
        
        return conversationRepository.save(conversation);
    }
    
    /**
     * Utility to extract list of strings from JSON array node.
     */
    private List<String> extractListFromJson(JsonNode arrayNode) {
        List<String> list = new ArrayList<>();
        if (arrayNode != null && arrayNode.isArray()) {
            arrayNode.forEach(node -> list.add(node.asText()));
        }
        return list;
    }
    
    /**
     * Get conversation history for a user.
     */
    public List<ConversationHistory> getConversationHistory(String userId, Long providerId) {
        if (providerId != null) {
            return conversationRepository.findByUserIdAndProviderIdOrderByTimestampDesc(userId, providerId);
        }
        return conversationRepository.findByUserIdOrderByTimestampDesc(userId);
    }
}
