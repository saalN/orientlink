package com.salvacode.orientlink.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.salvacode.orientlink.dto.ProviderResponseDTO;
import com.salvacode.orientlink.entity.ProviderProfile;
import com.salvacode.orientlink.repository.ProviderProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service for managing provider profiles.
 * Handles extraction from Alibaba URLs and CRUD operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProviderService {
    
    private final OpenAiIntegrationService openAiService;
    private final ProviderProfileRepository providerRepository;
    
    /**
     * Analyze Alibaba URL and extract provider information.
     * Saves or updates provider profile in database.
     */
    @Transactional
    public ProviderResponseDTO analyzeProvider(String alibabaUrl, String userId, String additionalContext) {
        log.info("Analyzing provider from URL: {}", alibabaUrl);
        
        // Check if provider already exists
        Optional<ProviderProfile> existingProvider = providerRepository.findByAlibabaUrl(alibabaUrl);
        
        // Call OpenAI for extraction
        String aiResponse = openAiService.extractProviderInfo(alibabaUrl, additionalContext);
        JsonNode jsonResponse = openAiService.parseJsonResponse(aiResponse);
        
        // Build or update provider profile
        ProviderProfile provider = existingProvider.orElse(new ProviderProfile());
        updateProviderFromJson(provider, jsonResponse, alibabaUrl, userId);
        
        // Save to database
        provider = providerRepository.save(provider);
        log.info("Provider profile saved with ID: {}", provider.getId());
        
        // Build response DTO
        return buildProviderResponse(provider, jsonResponse);
    }
    
    /**
     * Update provider entity from OpenAI JSON response.
     */
    private void updateProviderFromJson(ProviderProfile provider, JsonNode json, 
                                        String alibabaUrl, String userId) {
        provider.setUserId(userId);
        provider.setAlibabaUrl(alibabaUrl);
        provider.setProviderName(json.get("providerName").asText());
        provider.setProductName(json.get("productName").asText());
        
        // Handle nullable numeric fields
        if (!json.get("moq").isNull()) {
            provider.setMoq(json.get("moq").asInt());
        }
        if (!json.get("pricePerUnit").isNull()) {
            provider.setPricePerUnit(json.get("pricePerUnit").asDouble());
        }
        if (!json.get("deliveryTimeDays").isNull()) {
            provider.setDeliveryTimeDays(json.get("deliveryTimeDays").asInt());
        }
        
        provider.setCurrency(json.get("currency").asText());
        
        // Convert certifications array to comma-separated string
        List<String> certifications = new ArrayList<>();
        JsonNode certsNode = json.get("certifications");
        if (certsNode != null && certsNode.isArray()) {
            certsNode.forEach(node -> certifications.add(node.asText()));
        }
        provider.setCertifications(String.join(", ", certifications));
        
        provider.setAdditionalInfo(json.get("additionalInfo").asText());
        
        // Extract risk assessment
        JsonNode riskNode = json.get("riskAssessment");
        if (riskNode != null) {
            provider.setRiskAssessment(riskNode.toString());
        }
    }
    
    /**
     * Build ProviderResponse DTO from entity and JSON.
     */
    private ProviderResponseDTO buildProviderResponse(ProviderProfile provider, JsonNode json) {
        JsonNode riskNode = json.get("riskAssessment");
        
        List<String> warnings = new ArrayList<>();
        if (riskNode != null && riskNode.has("warnings")) {
            riskNode.get("warnings").forEach(node -> warnings.add(node.asText()));
        }
        
        ProviderResponseDTO.RiskAssessment riskAssessment = ProviderResponseDTO.RiskAssessment.builder()
                .overallRisk(riskNode != null ? riskNode.get("overallRisk").asText() : "unknown")
                .warnings(warnings)
                .recommendation(riskNode != null ? riskNode.get("recommendation").asText() : "")
                .build();
        
        List<String> certsList = new ArrayList<>();
        if (provider.getCertifications() != null && !provider.getCertifications().isEmpty()) {
            certsList = List.of(provider.getCertifications().split(", "));
        }
        
        return ProviderResponseDTO.builder()
                .providerId(provider.getId())
                .providerName(provider.getProviderName())
                .alibabaUrl(provider.getAlibabaUrl())
                .productName(provider.getProductName())
                .moq(provider.getMoq())
                .pricePerUnit(provider.getPricePerUnit())
                .currency(provider.getCurrency())
                .certifications(certsList)
                .deliveryTimeDays(provider.getDeliveryTimeDays())
                .additionalInfo(provider.getAdditionalInfo())
                .riskAssessment(riskAssessment)
                .analyzedAt(LocalDateTime.now())
                .build();
    }
    
    /**
     * Get all providers for a user.
     */
    public List<ProviderProfile> getUserProviders(String userId) {
        return providerRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
    
    /**
     * Get provider by ID.
     */
    public Optional<ProviderProfile> getProviderById(Long id) {
        return providerRepository.findById(id);
    }
    
    /**
     * Search providers by name.
     */
    public List<ProviderProfile> searchProvidersByName(String name) {
        return providerRepository.findByProviderNameContainingIgnoreCaseOrderByCreatedAtDesc(name);
    }
}
