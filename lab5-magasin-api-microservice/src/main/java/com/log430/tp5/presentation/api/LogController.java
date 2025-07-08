package com.log430.tp5.presentation.api;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Logs", description = "Accès aux logs de l'application")
@RestController
public class LogController {
    @Operation(summary = "Obtenir les logs backend", description = "Retourne les logs du backend sous forme de liste de chaînes.")
    @GetMapping("/api/logs")
    public ResponseEntity<List<String>> getLogs() {
        try {
            // Adjust the path to your log file if needed
            List<String> allLines = Files.readAllLines(Paths.get("logs/spring.log"));
            int from = Math.max(0, allLines.size() - 50); // last 50 lines
            return ResponseEntity.ok(allLines.subList(from, allLines.size()));
        } catch (Exception e) {
            return ResponseEntity
                .internalServerError()
                .body(Collections.singletonList("Could not read log file: " + e.getMessage()));
        }
    }
}
