package com.log430.tp2.repository;

// ProduitRepository.java
import org.springframework.data.jpa.repository.JpaRepository;

import com.log430.tp2.model.Produit;

public interface ProduitRepository extends JpaRepository<Produit, Integer> { }
