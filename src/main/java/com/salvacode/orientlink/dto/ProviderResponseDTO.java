package com.salvacode.orientlink.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Immutable response DTO for provider analysis from Alibaba URL.
 * Contains extracted business data like MOQ, pricing, certifications.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProviderResponseDTO {
    
    private Long providerId; // ID of saved provider profile
    
    @Size(max = 500, message = "Provider name cannot exceed 500 characters")
    private String providerName;
    
    @Size(max = 1000, message = "Alibaba URL cannot exceed 1000 characters")
    private String alibabaUrl;
    
    @Size(max = 500, message = "Product name cannot exceed 500 characters")
    private String productName;
    
    @Positive(message = "MOQ must be positive")
    private Integer moq;
    
    @Positive(message = "Price per unit must be positive")
    private Double pricePerUnit;
    
    @Size(max = 50, message = "Currency cannot exceed 50 characters")
    private String currency;
    
    private List<@NotBlank @Size(max = 100) String> certifications;
    
    @Positive(message = "Delivery time must be positive")
    private Integer deliveryTimeDays;
    
    @Size(max = 3000, message = "Additional info cannot exceed 3000 characters")
    private String additionalInfo;
    
    @Valid
    private RiskAssessment riskAssessment;
    
    @NotNull(message = "Analyzed timestamp cannot be null")
    private LocalDateTime analyzedAt;
    
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RiskAssessment {
        
        @Size(max = 50, message = "Overall risk cannot exceed 50 characters")
        private String overallRisk; // "low", "medium", "high"
        
        private List<@NotBlank @Size(max = 1000) String> warnings; // Specific warnings
        
        @Size(max = 1000, message = "Recommendation cannot exceed 1000 characters")
        private String recommendation; // OpenAI's recommendation
    }
}
