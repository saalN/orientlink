package com.salvacode.orientlink.repository;

import com.salvacode.orientlink.entity.ConversationHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for ConversationHistory entity.
 * Provides CRUD operations and custom queries for conversation tracking.
 */
@Repository
public interface ConversationHistoryRepository extends JpaRepository<ConversationHistory, Long> {
    
    /**
     * Find all conversations for a specific user, ordered by timestamp descending.
     */
    List<ConversationHistory> findByUserIdOrderByTimestampDesc(String userId);
    
    /**
     * Find conversations between a user and a specific provider.
     */
    List<ConversationHistory> findByUserIdAndProviderIdOrderByTimestampDesc(String userId, Long providerId);
    
    /**
     * Find recent conversations for a user (last N days).
     */
    List<ConversationHistory> findByUserIdAndTimestampAfterOrderByTimestampDesc(
            String userId, LocalDateTime since);
    
    /**
     * Find all conversations by message type (e.g., "analysis", "user_to_provider").
     */
    List<ConversationHistory> findByMessageTypeOrderByTimestampDesc(String messageType);
}
