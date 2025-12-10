package com.salvacode.orientlink.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Immutable response DTO for message analysis containing translation, interpretation,
 * alerts, and suggested responses in Chinese.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalyzeResponseDTO {
    
    @NotBlank(message = "Original message cannot be empty")
    @Size(max = 5000, message = "Original message cannot exceed 5000 characters")
    private String originalMessage;
    
    @Size(max = 5000, message = "Translated message cannot exceed 5000 characters")
    private String translatedMessage;
    
    @NotBlank(message = "Source language cannot be empty")
    @Size(max = 50, message = "Source language cannot exceed 50 characters")
    private String sourceLanguage;
    
    @NotBlank(message = "Target language cannot be empty")
    @Size(max = 50, message = "Target language cannot exceed 50 characters")
    private String targetLanguage;
    
    @Valid
    private InterpretationData interpretation;

    @NotBlank 
    @Size(max = 2000)
    private List<String> alerts; // Business warnings (MOQ, pricing, suspicious terms)
    
    @Valid
    private SuggestedResponses suggestedResponses;
    
    @NotNull(message = "Timestamp cannot be null")
    private LocalDateTime timestamp;
    
    private Long conversationId; // ID of saved conversation in DB
    
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InterpretationData {
        
        @Size(max = 3000, message = "Business context cannot exceed 3000 characters")
        private String businessContext; // General business interpretation
        
        @Size(max = 50, message = "Sentiment cannot exceed 50 characters")
        private String sentiment; // "positive", "neutral", "negative", "urgent"
        
        private List<@NotBlank @Size(max = 100) String> keyTerms; // MOQ, price, delivery time, etc.
        
        @Size(max = 50, message = "Risk level cannot exceed 50 characters")
        private String riskLevel; // "low", "medium", "high"
    }
    
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SuggestedResponses {
        
        @Size(max = 3000, message = "Formal response cannot exceed 3000 characters")
        private String formal; // Formal Chinese response
        
        @Size(max = 3000, message = "Negotiator response cannot exceed 3000 characters")
        private String negotiator; // Negotiating tone response
        
        @Size(max = 3000, message = "Direct response cannot exceed 3000 characters")
        private String direct; // Direct/assertive response
    }
}
