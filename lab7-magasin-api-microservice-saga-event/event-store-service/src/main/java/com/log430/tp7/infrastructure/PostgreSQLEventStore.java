package com.log430.tp7.infrastructure;

import com.log430.tp7.domain.Event;
import com.log430.tp7.domain.EventStore;
import com.log430.tp7.domain.OptimisticLockingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class PostgreSQLEventStore implements EventStore {
    
    private static final Logger logger = LoggerFactory.getLogger(PostgreSQLEventStore.class);
    
    private final EventRepository eventRepository;
    
    @Autowired
    public PostgreSQLEventStore(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }
    
    @Override
    public void saveEvent(Event event) {
        try {
            // Validate event version for optimistic concurrency control
            validateEventVersion(event);
            
            eventRepository.save(event);
            logger.info("Event saved successfully: {} for aggregate: {}", 
                       event.getEventType(), event.getAggregateId());
            
        } catch (DataIntegrityViolationException e) {
            // Handle unique constraint violation for aggregate_id + event_version
            if (e.getMessage().contains("unique_aggregate_version")) {
                int currentVersion = getLatestVersion(event.getAggregateId());
                throw new OptimisticLockingException(
                    event.getAggregateId(), 
                    event.getEventVersion(), 
                    currentVersion
                );
            }
            throw e;
        }
    }
    
    @Override
    public void saveEvents(List<Event> events) {
        if (events.isEmpty()) {
            return;
        }
        
        try {
            // Validate all events for optimistic concurrency control
            for (Event event : events) {
                validateEventVersion(event);
            }
            
            eventRepository.saveAll(events);
            logger.info("Batch saved {} events", events.size());
            
        } catch (DataIntegrityViolationException e) {
            if (e.getMessage().contains("unique_aggregate_version")) {
                // Find which event caused the conflict
                for (Event event : events) {
                    int currentVersion = getLatestVersion(event.getAggregateId());
                    if (currentVersion >= event.getEventVersion()) {
                        throw new OptimisticLockingException(
                            event.getAggregateId(), 
                            event.getEventVersion(), 
                            currentVersion
                        );
                    }
                }
            }
            throw e;
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Event> getEvents(String aggregateId) {
        logger.debug("Retrieving all events for aggregate: {}", aggregateId);
        return eventRepository.findByAggregateIdOrderByEventVersionAsc(aggregateId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Event> getEvents(String aggregateId, int fromVersion) {
        logger.debug("Retrieving events for aggregate: {} from version: {}", aggregateId, fromVersion);
        return eventRepository.findByAggregateIdAndEventVersionGreaterThanEqualOrderByEventVersionAsc(
            aggregateId, fromVersion);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Event> getEventsUpTo(String aggregateId, int toVersion) {
        logger.debug("Retrieving events for aggregate: {} up to version: {}", aggregateId, toVersion);
        return eventRepository.findByAggregateIdAndEventVersionLessThanEqualOrderByEventVersionAsc(
            aggregateId, toVersion);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Event> getEventsByType(String eventType) {
        logger.debug("Retrieving events by type: {}", eventType);
        return eventRepository.findByEventTypeOrderByTimestampAsc(eventType);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Event> getEventsByCorrelationId(UUID correlationId) {
        logger.debug("Retrieving events by correlation ID: {}", correlationId);
        return eventRepository.findByCorrelationIdOrderByTimestampAsc(correlationId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Event> getEventsByTimeRange(Instant from, Instant to) {
        logger.debug("Retrieving events between {} and {}", from, to);
        return eventRepository.findByTimestampBetweenOrderByTimestampAsc(from, to);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Event> getEventsForAggregates(List<String> aggregateIds) {
        logger.debug("Retrieving events for {} aggregates", aggregateIds.size());
        return eventRepository.findByAggregateIdInOrderByAggregateIdAscEventVersionAsc(aggregateIds);
    }
    
    @Override
    @Transactional(readOnly = true)
    public int getLatestVersion(String aggregateId) {
        Integer version = eventRepository.findMaxVersionByAggregateId(aggregateId);
        return version != null ? version : 0;
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean aggregateExists(String aggregateId) {
        return eventRepository.existsByAggregateId(aggregateId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<Event> getEventById(UUID eventId) {
        return eventRepository.findById(eventId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Event> getEventsWithPagination(int offset, int limit) {
        PageRequest pageRequest = PageRequest.of(offset / limit, limit);
        return eventRepository.findEventsWithPagination(pageRequest);
    }
    
    @Override
    @Transactional(readOnly = true)
    public long getTotalEventCount() {
        return eventRepository.count();
    }
    
    @Override
    @Transactional(readOnly = true)
    public long getEventCount(String aggregateId) {
        return eventRepository.countByAggregateId(aggregateId);
    }
    
    /**
     * Validate event version for optimistic concurrency control
     */
    private void validateEventVersion(Event event) {
        int currentVersion = getLatestVersion(event.getAggregateId());
        int expectedVersion = event.getEventVersion();
        
        // For new aggregates, version should be 1
        if (currentVersion == 0 && expectedVersion != 1) {
            throw new OptimisticLockingException(
                event.getAggregateId(), 1, expectedVersion);
        }
        
        // For existing aggregates, version should be currentVersion + 1
        if (currentVersion > 0 && expectedVersion != currentVersion + 1) {
            throw new OptimisticLockingException(
                event.getAggregateId(), currentVersion + 1, expectedVersion);
        }
    }
    
    /**
     * Get events for replay with proper ordering
     */
    @Transactional(readOnly = true)
    public List<Event> getEventsForReplay(String aggregateId) {
        logger.debug("Retrieving events for replay for aggregate: {}", aggregateId);
        return eventRepository.findEventsForReplay(aggregateId);
    }
    
    /**
     * Find events by multiple criteria
     */
    @Transactional(readOnly = true)
    public List<Event> findEventsByCriteria(String aggregateId, String eventType, 
                                           String aggregateType, Instant fromTime, Instant toTime) {
        logger.debug("Retrieving events by criteria - aggregateId: {}, eventType: {}, aggregateType: {}", 
                    aggregateId, eventType, aggregateType);
        return eventRepository.findEventsByCriteria(aggregateId, eventType, aggregateType, fromTime, toTime);
    }
}