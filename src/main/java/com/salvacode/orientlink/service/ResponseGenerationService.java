package com.salvacode.orientlink.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.salvacode.orientlink.dto.AnalyzeResponseDTO;
import com.salvacode.orientlink.dto.RespondRequestDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service for generating response suggestions in Chinese.
 * Uses OpenAI to create contextually appropriate responses in different tones.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ResponseGenerationService {
    
    private final OpenAiIntegrationService openAiService;
    
    /**
     * Generate Chinese response suggestions based on context and user intent.
     * 
     * @param request Request containing context, intent, and desired response type
     * @return Response with suggested Chinese messages in different tones
     */
    public AnalyzeResponseDTO.SuggestedResponses generateResponses(RespondRequestDTO request) {
        log.info("Generating {} response(s)", request.getResponseType());
        
        String responseType = request.getResponseType() != null ? request.getResponseType() : "all";
        
        // Call OpenAI to generate responses
        String aiResponse = openAiService.generateResponses(
                request.getContext(),
                request.getUserIntent(),
                responseType
        );
        
        // Parse JSON response
        JsonNode jsonResponse = openAiService.parseJsonResponse(aiResponse);
        JsonNode responses = jsonResponse.get("responses");
        
        // Build response DTO
        AnalyzeResponseDTO.SuggestedResponses.SuggestedResponsesBuilder builder = 
                AnalyzeResponseDTO.SuggestedResponses.builder();
        
        if (responses.has("formal") && !responses.get("formal").isNull()) {
            builder.formal(responses.get("formal").asText());
        }
        if (responses.has("negotiator") && !responses.get("negotiator").isNull()) {
            builder.negotiator(responses.get("negotiator").asText());
        }
        if (responses.has("direct") && !responses.get("direct").isNull()) {
            builder.direct(responses.get("direct").asText());
        }
        
        log.info("Response generation completed successfully");
        return builder.build();
    }
}
