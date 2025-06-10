package com.log430.tp2.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.log430.tp2.model.Magasin;

public interface MagasinRepository extends JpaRepository<Magasin, Integer> {
}
