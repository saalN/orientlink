package com.salvacode.orientlink.controller;

import com.salvacode.orientlink.dto.ProviderResponseDTO;
import com.salvacode.orientlink.dto.ProviderResponseDTO;
import com.salvacode.orientlink.entity.ProviderProfile;
import com.salvacode.orientlink.service.ProviderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for provider profile management.
 * 
 * Endpoints:
 * - GET /api/v1/provider: Analyze Alibaba URL and extract provider info
 * - GET /api/v1/providers: Get all providers for a user
 * - GET /api/v1/provider/{id}: Get specific provider by ID
 * - GET /api/v1/providers/search: Search providers by name
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*") // Configure properly in production
public class ProviderController {
    
    private final ProviderService providerService;
    
    /**
     * Analyze Alibaba provider URL and extract business information.
     * 
     * Example Request:
     * GET /api/v1/provider?url=https://www.alibaba.com/product-detail/...&userId=user123
     * 
     * Example Response:
     * {
     *   "providerId": 10,
     *   "providerName": "Shenzhen Tech Manufacturing Co.",
     *   "alibabaUrl": "https://www.alibaba.com/product-detail/...",
     *   "productName": "Wireless Bluetooth Earbuds",
     *   "moq": 500,
     *   "pricePerUnit": 3.50,
     *   "currency": "USD",
     *   "certifications": ["CE", "FCC", "RoHS"],
     *   "deliveryTimeDays": 30,
     *   "additionalInfo": "Factory with 10+ years experience, OEM/ODM available",
     *   "riskAssessment": {
     *     "overallRisk": "low",
     *     "warnings": [
     *       "Verify certifications before large order",
     *       "Request samples for quality check"
     *     ],
     *     "recommendation": "Reliable supplier with good track record. Proceed with sample order first."
     *   },
     *   "analyzedAt": "2025-12-01T10:30:00"
     * }
     */
    @GetMapping("/provider")
    public ResponseEntity<ProviderResponseDTO> analyzeProvider(
            @RequestParam String url,
            @RequestParam String userId,
            @RequestParam(required = false) String context) {
        log.info("Analyzing provider from URL for user: {}", userId);
        
        ProviderResponseDTO response = providerService.analyzeProvider(url, userId, context);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get all providers for a specific user.
     * 
     * Example Request:
     * GET /api/v1/providers?userId=user123
     * 
     * Returns list of ProviderProfile entities.
     */
    @GetMapping("/providers")
    public ResponseEntity<List<ProviderProfile>> getUserProviders(@RequestParam String userId) {
        log.info("Retrieving all providers for user: {}", userId);
        
        List<ProviderProfile> providers = providerService.getUserProviders(userId);
        
        return ResponseEntity.ok(providers);
    }
    
    /**
     * Get specific provider by ID.
     * 
     * Example Request:
     * GET /api/v1/provider/10
     * 
     * Returns ProviderProfile entity.
     */
    @GetMapping("/provider/{id}")
    public ResponseEntity<ProviderProfile> getProviderById(@PathVariable Long id) {
        log.info("Retrieving provider by ID: {}", id);
        
        return providerService.getProviderById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Search providers by name (partial match, case-insensitive).
     * 
     * Example Request:
     * GET /api/v1/providers/search?name=shenzhen
     * 
     * Returns list of matching ProviderProfile entities.
     */
    @GetMapping("/providers/search")
    public ResponseEntity<List<ProviderProfile>> searchProviders(@RequestParam String name) {
        log.info("Searching providers by name: {}", name);
        
        List<ProviderProfile> providers = providerService.searchProvidersByName(name);
        
        return ResponseEntity.ok(providers);
    }
}
