package com.log430.tp2.repository;

// EmployeRepository.java
import org.springframework.data.jpa.repository.JpaRepository;

import com.log430.tp2.model.Employe;

public interface EmployeRepository extends JpaRepository<Employe, Integer> {
    // For query
}
