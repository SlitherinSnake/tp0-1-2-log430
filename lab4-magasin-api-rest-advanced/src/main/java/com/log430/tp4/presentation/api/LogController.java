package com.log430.tp4.presentation.api;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LogController {
    @GetMapping("/api/logs")
    public List<String> getLogs() {
        try {
            // Adjust the path to your log file if needed
            List<String> allLines = Files.readAllLines(Paths.get("logs/spring.log"));
            int from = Math.max(0, allLines.size() - 50); // last 50 lines
            return allLines.subList(from, allLines.size());
        } catch (Exception e) {
            return Collections.singletonList("Could not read log file: " + e.getMessage());
        }
    }
}
