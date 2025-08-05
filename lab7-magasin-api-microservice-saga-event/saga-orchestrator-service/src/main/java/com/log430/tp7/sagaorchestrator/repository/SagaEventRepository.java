package com.log430.tp7.sagaorchestrator.repository;

import com.log430.tp7.sagaorchestrator.model.SagaEvent;
import com.log430.tp7.sagaorchestrator.model.SagaEventType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for SagaEvent entity with custom query methods.
 * Provides methods for querying saga events for audit trail and monitoring.
 */
@Repository
public interface SagaEventRepository extends JpaRepository<SagaEvent, String> {

       /**
        * Finds all events for a specific saga ordered by creation time.
        * 
        * @param sagaId the saga identifier
        * @return list of events for the saga
        */
       List<SagaEvent> findBySagaIdOrderByCreatedAtAsc(String sagaId);

       /**
        * Finds all events for a specific saga and event type.
        * 
        * @param sagaId    the saga identifier
        * @param eventType the event type to filter by
        * @return list of matching events
        */
       List<SagaEvent> findBySagaIdAndEventTypeOrderByCreatedAtAsc(String sagaId, SagaEventType eventType);

       /**
        * Finds events by event type within a time range.
        * 
        * @param eventType the event type to filter by
        * @param startTime the start of the time range
        * @param endTime   the end of the time range
        * @return list of matching events
        */
       List<SagaEvent> findByEventTypeAndCreatedAtBetweenOrderByCreatedAtDesc(
                     SagaEventType eventType, LocalDateTime startTime, LocalDateTime endTime);

       /**
        * Finds recent events for a saga (last N events).
        * 
        * @param sagaId the saga identifier
        * @param limit  the maximum number of events to return
        * @return list of recent events
        */
       @Query("SELECT e FROM SagaEvent e WHERE e.sagaId = :sagaId ORDER BY e.createdAt DESC LIMIT :limit")
       List<SagaEvent> findRecentEventsBySagaId(@Param("sagaId") String sagaId, @Param("limit") int limit);

       /**
        * Finds all error events within a time range.
        * 
        * @param startTime the start of the time range
        * @param endTime   the end of the time range
        * @return list of error events
        */
       @Query("SELECT e FROM SagaEvent e WHERE e.eventType = 'ERROR' AND e.createdAt BETWEEN :startTime AND :endTime ORDER BY e.createdAt DESC")
       List<SagaEvent> findErrorEventsBetween(@Param("startTime") LocalDateTime startTime,
                     @Param("endTime") LocalDateTime endTime);

       /**
        * Finds all service call failed events for a specific service.
        * 
        * @param serviceName the name of the service
        * @param startTime   the start of the time range
        * @param endTime     the end of the time range
        * @return list of service call failed events
        */
       @Query("SELECT e FROM SagaEvent e WHERE e.eventType = 'SERVICE_CALL_FAILED' " +
                     "AND JSON_EXTRACT(e.eventData, '$.serviceName') = :serviceName " +
                     "AND e.createdAt BETWEEN :startTime AND :endTime ORDER BY e.createdAt DESC")
       List<SagaEvent> findServiceCallFailuresByService(@Param("serviceName") String serviceName,
                     @Param("startTime") LocalDateTime startTime,
                     @Param("endTime") LocalDateTime endTime);

       /**
        * Counts events by type within a time range.
        * 
        * @param eventType the event type to count
        * @param startTime the start of the time range
        * @param endTime   the end of the time range
        * @return count of events
        */
       long countByEventTypeAndCreatedAtBetween(SagaEventType eventType, LocalDateTime startTime,
                     LocalDateTime endTime);

       /**
        * Finds all compensation events for failed sagas.
        * 
        * @param startTime the start of the time range
        * @param endTime   the end of the time range
        * @return list of compensation events
        */
       @Query("SELECT e FROM SagaEvent e WHERE e.eventType IN ('COMPENSATION_STARTED', 'COMPENSATION_COMPLETED') " +
                     "AND e.createdAt BETWEEN :startTime AND :endTime ORDER BY e.createdAt DESC")
       List<SagaEvent> findCompensationEventsBetween(@Param("startTime") LocalDateTime startTime,
                     @Param("endTime") LocalDateTime endTime);

       /**
        * Finds events for sagas that took longer than specified duration.
        * 
        * @param cutoffTime the cutoff time (current time minus duration)
        * @return list of events for long-running sagas
        */
       @Query("SELECT e FROM SagaEvent e WHERE e.eventType = 'SAGA_COMPLETED' " +
                     "AND e.sagaId IN (SELECT s.sagaId FROM SagaEvent s WHERE s.eventType = 'SAGA_STARTED' " +
                     "AND s.createdAt <= :cutoffTime) " +
                     "ORDER BY e.createdAt DESC")
       List<SagaEvent> findLongRunningSagaEvents(@Param("cutoffTime") LocalDateTime cutoffTime);

       /**
        * Deletes old events beyond the retention period.
        * 
        * @param cutoffDate the date before which events should be deleted
        * @return number of deleted events
        */
       @Query("DELETE FROM SagaEvent e WHERE e.createdAt < :cutoffDate")
       int deleteEventsOlderThan(@Param("cutoffDate") LocalDateTime cutoffDate);

       /**
        * Finds the latest event for each saga.
        * 
        * @return list of latest events per saga
        */
       @Query("SELECT e FROM SagaEvent e WHERE e.createdAt = " +
                     "(SELECT MAX(e2.createdAt) FROM SagaEvent e2 WHERE e2.sagaId = e.sagaId)")
       List<SagaEvent> findLatestEventPerSaga();
}