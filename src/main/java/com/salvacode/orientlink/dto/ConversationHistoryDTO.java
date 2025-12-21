package com.salvacode.orientlink.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationHistoryDTO {
    
    private String userId;
    
    private Long providerId;
    
    private String originalMessage;

    private String translatedMessage;
    
    private String sourceLanguage; 
    
    private String targetLanguage; 
    
    private String aiInterpretation; 
    
    private String alerts; 
    
    private String suggestedResponses; 

    private String messageType; 
    
    
}
