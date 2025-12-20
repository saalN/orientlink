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

        AnalyzeResponseDTO.SuggestedResponses.SuggestedResponsesBuilder builder = AnalyzeResponseDTO.SuggestedResponses.builder();

        if (responses.has("formal") && !responses.get("formal").isNull()) {
            JsonNode formal = responses.get("formal");
            builder.formal(AnalyzeResponseDTO.BilingualResponse.builder()
                .zh(formal.get("zh").asText())
                .es(formal.get("es").asText())
                .build());
        }
        if (responses.has("negotiator") && !responses.get("negotiator").isNull()) {
            JsonNode negotiator = responses.get("negotiator");
            builder.negotiator(AnalyzeResponseDTO.BilingualResponse.builder()
                .zh(negotiator.get("zh").asText())
                .es(negotiator.get("es").asText())
                .build());
        }
        if (responses.has("direct") && !responses.get("direct").isNull()) {
            JsonNode direct = responses.get("direct");
            builder.direct(AnalyzeResponseDTO.BilingualResponse.builder()
                .zh(direct.get("zh").asText())
                .es(direct.get("es").asText())
                .build());
        }

        log.info("Response generation completed successfully");
        return builder.build();
    }
}
