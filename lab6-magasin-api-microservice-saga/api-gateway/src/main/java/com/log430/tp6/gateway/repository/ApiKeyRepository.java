package com.log430.tp6.gateway.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.log430.tp6.gateway.model.ApiKeyEntity;

@Repository
public interface ApiKeyRepository extends JpaRepository<ApiKeyEntity, Long> {
    Optional<ApiKeyEntity> findByApiKeyAndClientId(String apiKey, String clientId);
    Optional<ApiKeyEntity> findByApiKey(String apiKey);
}
