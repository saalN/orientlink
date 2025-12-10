package com.salvacode.orientlink.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Immutable request DTO for analyzing user messages about provider conversations.
 * Contains the message text and optional context about the provider.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalyzeRequestDTO{
    
    @NotBlank(message = "Message text cannot be empty")
    @Size(max = 5000, message = "Message text cannot exceed 5000 characters")
    private String messageText;
    
    private String sourceLanguage; // "es" or "zh", auto-detect if null
    
    private String targetLanguage; // "es" or "zh", default opposite of source if null
    
    private Long providerId; // Optional: link to existing provider profile
    
    @NotNull(message = "User ID is required")
    @NotBlank(message = "User ID cannot be empty")
    private String userId; // User identifier for tracking conversations
    
    @Size(max = 3000, message = "Conversation context cannot exceed 3000 characters")
    private String conversationContext; // Optional: previous conversation summary
}
