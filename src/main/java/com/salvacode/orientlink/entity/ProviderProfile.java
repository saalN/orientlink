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
 * Entity to store provider profiles extracted from Alibaba or manually created.
 * Contains business information like MOQ, pricing, certifications, and delivery time.
 */
@Entity
@Table(name = "provider_profile")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProviderProfile {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;
    
    @Column(nullable = false)
    private String userId;
    
    @Column(length = 500)
    private String providerName;
    
    @Column(length = 1000)
    private String alibabaUrl;
    
    @Column(length = 500)
    private String productName;
    
    @Column
    private Integer moq; // Minimum Order Quantity
    
    @Column
    private Double pricePerUnit;
    
    @Column(length = 50)
    private String currency; // "USD", "CNY", etc.
    
    @Column(length = 1000)
    private String certifications; // JSON array or comma-separated
    
    @Column
    private Integer deliveryTimeDays;
    
    @Column(length = 3000)
    private String additionalInfo; // Any extra info extracted by OpenAI
    
    @Column(length = 1000)
    private String riskAssessment; // OpenAI's risk analysis
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Column
    private LocalDateTime updatedAt;
    
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
