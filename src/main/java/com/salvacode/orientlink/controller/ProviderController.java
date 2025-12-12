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
     */
    @GetMapping("/providers")
    public ResponseEntity<List<ProviderProfile>> getUserProviders(@RequestParam String userId) {
        log.info("Retrieving all providers for user: {}", userId);
        
        List<ProviderProfile> providers = providerService.getUserProviders(userId);
        
        return ResponseEntity.ok(providers);
    }
    
    /**
     * Get specific provider by ID.
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
    */
    @GetMapping("/providers/search")
    public ResponseEntity<List<ProviderProfile>> searchProviders(@RequestParam String name) {
        log.info("Searching providers by name: {}", name);
        
        List<ProviderProfile> providers = providerService.searchProvidersByName(name);
        
        return ResponseEntity.ok(providers);
    }
}
