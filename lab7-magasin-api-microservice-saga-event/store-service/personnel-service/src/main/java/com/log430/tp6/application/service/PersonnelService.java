package com.log430.tp7.application.service;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.log430.tp7.domain.personnel.Personnel;
import com.log430.tp7.domain.personnel.Role;
import com.log430.tp7.infrastructure.repository.PersonnelRepository;

/**
 * Application service for personnel management operations.
 * Coordinates domain logic and data access for personnel.
 */
@Service
@Transactional
public class PersonnelService {
    
    private static final Logger log = LoggerFactory.getLogger(PersonnelService.class);
    
    private static final String PERSONNEL_NOT_FOUND_MSG = "Personnel not found with id: ";
    
    private final PersonnelRepository personnelRepository;
    
    public PersonnelService(PersonnelRepository personnelRepository) {
        this.personnelRepository = personnelRepository;
    }
    
    /**
     * Get all active personnel.
     */
    @Transactional(readOnly = true)
    @Cacheable("activePersonnel")
    public List<Personnel> getAllActivePersonnel() {
        log.info("Fetching all active personnel");
        List<Personnel> personnel = personnelRepository.findByIsActiveTrue();
        log.info("Found {} active personnel", personnel.size());
        return personnel;
    }
    
    /**
     * Get personnel by ID.
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "personnelById", key = "#id")
    public Optional<Personnel> getPersonnelById(Long id) {
        log.info("Fetching personnel by id: {}", id);
        return personnelRepository.findById(id);
    }
    
    /**
     * Create new personnel.
     */
    @Caching(evict = {
        @CacheEvict(value = "activePersonnel", allEntries = true),
        @CacheEvict(value = "personnelById", key = "#result.id", condition = "#result != null")
    })
    public Personnel createPersonnel(String nom, String identifiant) {
        log.info("Creating new personnel: {}", nom);
        
        // Check if identifiant already exists
        if (personnelRepository.existsByIdentifiant(identifiant)) {
            throw new IllegalArgumentException("Personnel with identifiant '" + identifiant + "' already exists");
        }
        
        Personnel personnel = new Personnel(nom, identifiant);
        Personnel savedPersonnel = personnelRepository.save(personnel);
        log.info("Created personnel with id: {}", savedPersonnel.getId());
        return savedPersonnel;
    }
    
    /**
     * Create personnel with login access.
     */
    @Caching(evict = {
        @CacheEvict(value = "activePersonnel", allEntries = true),
        @CacheEvict(value = "personnelById", key = "#result.id", condition = "#result != null"),
        @CacheEvict(value = "personnelWithLogin", allEntries = true)
    })
    public Personnel createPersonnelWithLogin(String nom, String identifiant, String username, String password) {
        log.info("Creating new personnel with login: {}", nom);
        
        // Check if identifiant or username already exists
        if (personnelRepository.existsByIdentifiant(identifiant)) {
            throw new IllegalArgumentException("Personnel with identifiant '" + identifiant + "' already exists");
        }
        if (personnelRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Personnel with username '" + username + "' already exists");
        }
        
        Personnel personnel = new Personnel(nom, identifiant, username, password);
        Personnel savedPersonnel = personnelRepository.save(personnel);
        log.info("Created personnel with login, id: {}", savedPersonnel.getId());
        return savedPersonnel;
    }
    
    /**
     * Update personnel information.
     */
    @Caching(evict = {
        @CacheEvict(value = "activePersonnel", allEntries = true),
        @CacheEvict(value = "personnelById", key = "#id"),
        @CacheEvict(value = "personnelWithLogin", allEntries = true)
    })
    public Personnel updatePersonnel(Long id, String nom, String identifiant, String username) {
        log.info("Updating personnel with id: {}", id);
        return personnelRepository.findById(id)
                .map(personnel -> {
                    // Check if new identifiant conflicts with existing
                    if (!personnel.getIdentifiant().equals(identifiant) && 
                        personnelRepository.existsByIdentifiant(identifiant)) {
                        throw new IllegalArgumentException("Personnel with identifiant '" + identifiant + "' already exists");
                    }
                    
                    // Check if new username conflicts with existing
                    if (username != null && !username.equals(personnel.getUsername()) && 
                        personnelRepository.existsByUsername(username)) {
                        throw new IllegalArgumentException("Personnel with username '" + username + "' already exists");
                    }
                    
                    personnel.setNom(nom);
                    personnel.setIdentifiant(identifiant);
                    personnel.setUsername(username);
                    return personnelRepository.save(personnel);
                })
                .orElseThrow(() -> new IllegalArgumentException(PERSONNEL_NOT_FOUND_MSG + id));
    }
    
    /**
     * Set password for personnel.
     */
    @Caching(evict = {
        @CacheEvict(value = "personnelById", key = "#id"),
        @CacheEvict(value = "personnelWithLogin", allEntries = true)
    })
    public Personnel setPassword(Long id, String password) {
        log.info("Setting password for personnel with id: {}", id);
        return personnelRepository.findById(id)
                .map(personnel -> {
                    personnel.setPassword(password);
                    return personnelRepository.save(personnel);
                })
                .orElseThrow(() -> new IllegalArgumentException(PERSONNEL_NOT_FOUND_MSG + id));
    }
    
    /**
     * Add role to personnel.
     */
    @Caching(evict = {
        @CacheEvict(value = "personnelById", key = "#id"),
        @CacheEvict(value = "personnelByRole", allEntries = true)
    })
    public Personnel addRole(Long id, Role role) {
        log.info("Adding role {} to personnel with id: {}", role.getName(), id);
        return personnelRepository.findById(id)
                .map(personnel -> {
                    personnel.addRole(role);
                    return personnelRepository.save(personnel);
                })
                .orElseThrow(() -> new IllegalArgumentException(PERSONNEL_NOT_FOUND_MSG + id));
    }
    
    /**
     * Remove role from personnel.
     */
    @Caching(evict = {
        @CacheEvict(value = "personnelById", key = "#id"),
        @CacheEvict(value = "personnelByRole", allEntries = true)
    })
    public Personnel removeRole(Long id, Role role) {
        log.info("Removing role {} from personnel with id: {}", role.getName(), id);
        return personnelRepository.findById(id)
                .map(personnel -> {
                    personnel.removeRole(role);
                    return personnelRepository.save(personnel);
                })
                .orElseThrow(() -> new IllegalArgumentException(PERSONNEL_NOT_FOUND_MSG + id));
    }
    
    /**
     * Deactivate personnel.
     */
    @Caching(evict = {
        @CacheEvict(value = "activePersonnel", allEntries = true),
        @CacheEvict(value = "personnelById", key = "#id"),
        @CacheEvict(value = "personnelWithLogin", allEntries = true),
        @CacheEvict(value = "personnelByRole", allEntries = true)
    })
    public void deactivatePersonnel(Long id) {
        log.info("Deactivating personnel with id: {}", id);
        personnelRepository.findById(id)
                .ifPresentOrElse(
                        personnel -> {
                            personnel.setActive(false);
                            personnelRepository.save(personnel);
                            log.info("Personnel {} deactivated successfully", id);
                        },
                        () -> {
                            log.error("Personnel not found for deactivation: {}", id);
                            throw new IllegalArgumentException(PERSONNEL_NOT_FOUND_MSG + id);
                        }
                );
    }
    
    /**
     * Activate personnel.
     */
    @Caching(evict = {
        @CacheEvict(value = "activePersonnel", allEntries = true),
        @CacheEvict(value = "personnelById", key = "#id"),
        @CacheEvict(value = "personnelWithLogin", allEntries = true),
        @CacheEvict(value = "personnelByRole", allEntries = true)
    })
    public void activatePersonnel(Long id) {
        log.info("Activating personnel with id: {}", id);
        personnelRepository.findById(id)
                .ifPresentOrElse(
                        personnel -> {
                            personnel.setActive(true);
                            personnelRepository.save(personnel);
                            log.info("Personnel {} activated successfully", id);
                        },
                        () -> {
                            log.error("Personnel not found for activation: {}", id);
                            throw new IllegalArgumentException(PERSONNEL_NOT_FOUND_MSG + id);
                        }
                );
    }
    
    /**
     * Find personnel by username for authentication.
     */
    @Transactional(readOnly = true)
    public Optional<Personnel> findByUsername(String username) {
        log.info("Finding personnel by username: {}", username);
        return personnelRepository.findByUsernameAndIsActiveTrue(username);
    }
    
    /**
     * Find personnel by employee identifier.
     */
    @Transactional(readOnly = true)
    public Optional<Personnel> findByIdentifiant(String identifiant) {
        log.info("Finding personnel by identifiant: {}", identifiant);
        return personnelRepository.findByIdentifiantAndIsActiveTrue(identifiant);
    }
    
    /**
     * Get personnel with login access.
     */
    @Transactional(readOnly = true)
    @Cacheable("personnelWithLogin")
    public List<Personnel> getPersonnelWithLoginAccess() {
        log.info("Fetching personnel with login access");
        return personnelRepository.findPersonnelWithLoginAccess();
    }
    
    /**
     * Find personnel by role name.
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "personnelByRole", key = "#roleName")
    public List<Personnel> findByRoleName(String roleName) {
        log.info("Finding personnel by role: {}", roleName);
        return personnelRepository.findByRoleName(roleName);
    }
    
    /**
     * Get all personnel (including inactive ones).
     */
    @Transactional(readOnly = true)
    public List<Personnel> getAllPersonnel() {
        log.info("Fetching all personnel");
        return personnelRepository.findAll();
    }
    
    /**
     * Check if personnel exists and is active.
     */
    @Transactional(readOnly = true)
    public boolean isPersonnelActive(Long id) {
        log.info("Checking if personnel {} is active", id);
        return personnelRepository.findById(id)
                .map(Personnel::isActive)
                .orElse(false);
    }
    
    /**
     * Get personnel count.
     */
    @Transactional(readOnly = true)
    public long getPersonnelCount() {
        log.info("Counting total personnel");
        return personnelRepository.count();
    }
    
    /**
     * Get active personnel count.
     */
    @Transactional(readOnly = true)
    public long getActivePersonnelCount() {
        log.info("Counting active personnel");
        return personnelRepository.findByIsActiveTrue().size();
    }
}
