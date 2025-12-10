package com.salvacode.orientlink.repository;

import com.salvacode.orientlink.entity.ProviderProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for ProviderProfile entity.
 * Provides CRUD operations and custom queries for provider management.
 */
@Repository
public interface ProviderProfileRepository extends JpaRepository<ProviderProfile, Long> {
    
    /**
     * Find all providers for a specific user.
     */
    List<ProviderProfile> findByUserIdOrderByCreatedAtDesc(String userId);
    
    /**
     * Find a provider by Alibaba URL (for duplicate detection).
     */
    Optional<ProviderProfile> findByAlibabaUrl(String alibabaUrl);
    
    /**
     * Find providers by name (partial match, case-insensitive).
     */
    List<ProviderProfile> findByProviderNameContainingIgnoreCaseOrderByCreatedAtDesc(String name);
    
    /**
     * Find all providers for a user with a specific product name.
     */
    List<ProviderProfile> findByUserIdAndProductNameContainingIgnoreCaseOrderByCreatedAtDesc(
            String userId, String productName);
}
