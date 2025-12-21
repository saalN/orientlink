package com.salvacode.orientlink.controller;

import com.salvacode.orientlink.dto.AnalyzeRequestDTO;
import com.salvacode.orientlink.dto.AnalyzeResponseDTO;
import com.salvacode.orientlink.dto.ConversationHistoryDTO;
import com.salvacode.orientlink.dto.RespondRequestDTO;
import com.salvacode.orientlink.entity.ConversationHistory;
import com.salvacode.orientlink.service.AnalysisService;
import com.salvacode.orientlink.service.ResponseGenerationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for message analysis and response generation.
 * 
 * Endpoints:
 * - POST /api/v1/analyze: Analyze user message (translate, interpret, alert)
 * - POST /api/v1/respond: Generate suggested Chinese responses
 * - GET /api/v1/conversations: Get conversation history
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*") // Configure properly in production
public class AnalysisController {
    
    private final AnalysisService analysisService;
    private final ResponseGenerationService responseGenerationService;
    
    /**
     * Analyze a message from user or provider.
     */
    @PostMapping("/analyze")
    public ResponseEntity<AnalyzeResponseDTO> analyzeMessage(@Valid @RequestBody AnalyzeRequestDTO request) {
        log.info("Received analyze request from user: {}", request.getUserId());
        
        AnalyzeResponseDTO response = analysisService.analyzeMessage(request);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Generate suggested responses in Chinese based on context.
     */
    @PostMapping("/respond")
    public ResponseEntity<AnalyzeResponseDTO.SuggestedResponses> generateResponses(
            @Valid @RequestBody RespondRequestDTO request) {
        log.info("Received response generation request from user: {}", request.getUserId());
        
        AnalyzeResponseDTO.SuggestedResponses responses = responseGenerationService.generateResponses(request);
        
        return ResponseEntity.ok(responses);
    }
    
    /**
     * Get conversation history for a user.
     */
    @GetMapping("/conversations")
    public ResponseEntity<List<ConversationHistoryDTO>> getConversationHistory(
            @RequestParam String userId,
            @RequestParam(required = false) Long providerId) {
        log.info("Retrieving conversation history for user: {}", userId);
        
        List<ConversationHistoryDTO> history = analysisService.getConversationHistory(userId, providerId);
        
        return ResponseEntity.ok(history);
    }
}
