package com.salvacode.orientlink.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Immutable request DTO for generating suggested responses in Chinese.
 * Takes business context and desired tone to generate appropriate responses.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RespondRequestDTO {
    
    @NotBlank(message = "Context cannot be empty")
    @Size(max = 3000, message = "Context cannot exceed 3000 characters")
    private String context; // Business context or provider message
    
    @Size(max = 50, message = "Response type cannot exceed 50 characters")
    private String responseType; // "formal", "negotiator", "direct", or "all"
    
    @Size(max = 1000, message = "User intent cannot exceed 1000 characters")
    private String userIntent; // What user wants to communicate
    
    private Long providerId; // Optional: link to provider for context
    
    @NotNull(message = "User ID is required")
    @NotBlank(message = "User ID cannot be empty")
    private String userId; // User identifier
}
