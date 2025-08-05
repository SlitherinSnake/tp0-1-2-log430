package com.log430.tp7.sagaorchestrator.controller;

import com.log430.tp7.sagaorchestrator.dto.ErrorResponse;
import com.log430.tp7.sagaorchestrator.dto.SagaResponse;
import com.log430.tp7.sagaorchestrator.dto.SagaStatus;
import com.log430.tp7.sagaorchestrator.dto.SaleRequest;
import com.log430.tp7.sagaorchestrator.exception.SagaNotFoundException;
import com.log430.tp7.sagaorchestrator.model.SagaExecution;
import com.log430.tp7.sagaorchestrator.repository.SagaExecutionRepository;
import com.log430.tp7.sagaorchestrator.service.SagaOrchestrator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * REST controller for saga orchestration operations.
 * Provides endpoints for initiating sales sagas and checking their status.
 */
@RestController
@RequestMapping("/api/v1/saga")
@Tag(name = "Saga Orchestrator", description = "Endpoints for managing distributed transaction sagas")
@Validated
public class SagaController {
    
    private static final Logger logger = LoggerFactory.getLogger(SagaController.class);
    
    private final SagaOrchestrator sagaOrchestrator;
    private final SagaExecutionRepository sagaExecutionRepository;
    
    @Autowired
    public SagaController(SagaOrchestrator sagaOrchestrator, SagaExecutionRepository sagaExecutionRepository) {
        this.sagaOrchestrator = sagaOrchestrator;
        this.sagaExecutionRepository = sagaExecutionRepository;
    }
    
    /**
     * Initiates a new saga for a customer sale transaction.
     * Coordinates stock verification, reservation, payment, and order confirmation.
     * 
     * @param request The sale request containing customer and product details
     * @return SagaResponse with saga ID and current state
     */
    @PostMapping("/sales")
    @Operation(
        summary = "Create a new sale saga",
        description = "Initiates a distributed transaction saga for a customer sale, coordinating stock verification, reservation, payment processing, and order confirmation across multiple microservices."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201", 
            description = "Saga created successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = SagaResponse.class))
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "Invalid request data or validation errors",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "500", 
            description = "Internal server error during saga execution",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public ResponseEntity<SagaResponse> createSale(
            @Parameter(description = "Sale request with customer, product, and payment details", required = true)
            @Valid @RequestBody SaleRequest request) {
        
        logger.info("Received sale request: customerId={}, productId={}, quantity={}, amount={}", 
                   request.customerId(), request.productId(), request.quantity(), request.amount());
        
        try {
            // Execute the saga through the orchestrator
            SagaResponse response = sagaOrchestrator.executeSale(request);
            
            // Determine HTTP status based on saga outcome
            HttpStatus status = switch (response.state()) {
                case SALE_CONFIRMED -> HttpStatus.CREATED;
                case SALE_FAILED -> HttpStatus.BAD_REQUEST;
                default -> HttpStatus.ACCEPTED; // For intermediate states
            };
            
            logger.info("Sale saga response: sagaId={}, state={}, status={}", 
                       response.sagaId(), response.state(), status);
            
            return ResponseEntity.status(status).body(response);
            
        } catch (Exception e) {
            logger.error("Unexpected error during saga creation: customerId={}, productId={}, error={}", 
                        request.customerId(), request.productId(), e.getMessage(), e);
            
            // Return generic error response
            SagaResponse errorResponse = SagaResponse.failure(
                null, 
                null, 
                "Internal server error: " + e.getMessage()
            );
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Retrieves the current status of a saga by its ID.
     * 
     * @param sagaId The unique identifier of the saga
     * @return SagaStatus with detailed saga information
     */
    @GetMapping("/sales/{sagaId}")
    @Operation(
        summary = "Get saga status",
        description = "Retrieves the current status and details of a saga transaction by its unique identifier."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Saga status retrieved successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = SagaStatus.class))
        ),
        @ApiResponse(
            responseCode = "404", 
            description = "Saga not found",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "500", 
            description = "Internal server error",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public ResponseEntity<SagaStatus> getSagaStatus(
            @Parameter(description = "Unique identifier of the saga", required = true)
            @PathVariable @NotBlank(message = "Saga ID cannot be blank") String sagaId) {
        
        logger.info("Received saga status request: sagaId={}", sagaId);
        
        // Find the saga execution by ID
        Optional<SagaExecution> sagaOptional = sagaExecutionRepository.findById(sagaId);
        
        if (sagaOptional.isEmpty()) {
            logger.warn("Saga not found: sagaId={}", sagaId);
            throw new SagaNotFoundException(sagaId);
        }
        
        SagaExecution saga = sagaOptional.get();
        
        // Convert to SagaStatus DTO
        SagaStatus status = new SagaStatus(
            saga.getSagaId(),
            saga.getCurrentState(),
            saga.getCustomerId(),
            saga.getProductId(),
            saga.getQuantity(),
            saga.getAmount(),
            saga.getStockReservationId(),
            saga.getPaymentTransactionId(),
            saga.getOrderId(),
            saga.getCreatedAt(),
            saga.getUpdatedAt(),
            saga.getErrorMessage()
        );
        
        logger.info("Saga status retrieved: sagaId={}, state={}, customerId={}", 
                   sagaId, saga.getCurrentState(), saga.getCustomerId());
        
        return ResponseEntity.ok(status);
    }
}