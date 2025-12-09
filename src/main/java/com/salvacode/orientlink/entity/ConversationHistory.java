package com.salvacode.orientlink.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Entity to store the conversation history between users and Chinese providers.
 * Tracks all messages, translations, and AI interpretations for audit and context.
 */
@Entity
@Table(name = "conversation_history")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationHistory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;
    
    @Column(nullable = false)
    private String userId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_id")
    private ProviderProfile provider;
    
    @Column(nullable = false, length = 5000)
    private String originalMessage;
    
    @Column(length = 5000)
    private String translatedMessage;
    
    @Column(length = 50)
    private String sourceLanguage; // "es" or "zh"
    
    @Column(length = 50)
    private String targetLanguage; // "es" or "zh"
    
    @Column(length = 3000)
    private String aiInterpretation; // OpenAI's business context analysis
    
    @Column(length = 2000)
    private String alerts; // Warnings about MOQ, pricing, suspicious terms, etc.
    
    @Column(length = 3000)
    private String suggestedResponses; // JSON array of formal, negotiator, direct responses
    
    @Column(nullable = false)
    private LocalDateTime timestamp;
    
    @Column(length = 100)
    private String messageType; // "user_to_provider", "provider_to_user", "analysis"
    
    @PrePersist
    protected void onCreate() {
        timestamp = LocalDateTime.now();
    }
}
