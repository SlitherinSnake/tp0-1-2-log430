package com.log430.tp7.infrastructure;

import com.log430.tp7.domain.Event;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface EventRepository extends JpaRepository<Event, UUID> {
    
    /**
     * Find all events for a specific aggregate ordered by version
     */
    List<Event> findByAggregateIdOrderByEventVersionAsc(String aggregateId);
    
    /**
     * Find events for a specific aggregate starting from a version
     */
    List<Event> findByAggregateIdAndEventVersionGreaterThanEqualOrderByEventVersionAsc(
            String aggregateId, Integer fromVersion);
    
    /**
     * Find events for a specific aggregate up to a version
     */
    List<Event> findByAggregateIdAndEventVersionLessThanEqualOrderByEventVersionAsc(
            String aggregateId, Integer toVersion);
    
    /**
     * Find events by type ordered by timestamp
     */
    List<Event> findByEventTypeOrderByTimestampAsc(String eventType);
    
    /**
     * Find events by correlation ID ordered by timestamp
     */
    List<Event> findByCorrelationIdOrderByTimestampAsc(UUID correlationId);
    
    /**
     * Find events within a time range ordered by timestamp
     */
    List<Event> findByTimestampBetweenOrderByTimestampAsc(Instant from, Instant to);
    
    /**
     * Find events for multiple aggregates
     */
    List<Event> findByAggregateIdInOrderByAggregateIdAscEventVersionAsc(List<String> aggregateIds);
    
    /**
     * Get the maximum version for an aggregate
     */
    @Query("SELECT COALESCE(MAX(e.eventVersion), 0) FROM Event e WHERE e.aggregateId = :aggregateId")
    Integer findMaxVersionByAggregateId(@Param("aggregateId") String aggregateId);
    
    /**
     * Check if an aggregate exists
     */
    boolean existsByAggregateId(String aggregateId);
    
    /**
     * Count events for a specific aggregate
     */
    long countByAggregateId(String aggregateId);
    
    /**
     * Find events with pagination ordered by timestamp
     */
    @Query("SELECT e FROM Event e ORDER BY e.timestamp ASC")
    List<Event> findEventsWithPagination(Pageable pageable);
    
    /**
     * Find events by aggregate type
     */
    List<Event> findByAggregateTypeOrderByTimestampAsc(String aggregateType);
    
    /**
     * Find events by causation ID
     */
    List<Event> findByCausationIdOrderByTimestampAsc(UUID causationId);
    
    /**
     * Custom query to find events for replay with proper ordering
     */
    @Query("SELECT e FROM Event e WHERE e.aggregateId = :aggregateId " +
           "ORDER BY e.eventVersion ASC, e.timestamp ASC")
    List<Event> findEventsForReplay(@Param("aggregateId") String aggregateId);
    
    /**
     * Find events by multiple criteria for complex filtering
     */
    @Query("SELECT e FROM Event e WHERE " +
           "(:aggregateId IS NULL OR e.aggregateId = :aggregateId) AND " +
           "(:eventType IS NULL OR e.eventType = :eventType) AND " +
           "(:aggregateType IS NULL OR e.aggregateType = :aggregateType) AND " +
           "(:fromTime IS NULL OR e.timestamp >= :fromTime) AND " +
           "(:toTime IS NULL OR e.timestamp <= :toTime) " +
           "ORDER BY e.timestamp ASC")
    List<Event> findEventsByCriteria(
            @Param("aggregateId") String aggregateId,
            @Param("eventType") String eventType,
            @Param("aggregateType") String aggregateType,
            @Param("fromTime") Instant fromTime,
            @Param("toTime") Instant toTime);
}