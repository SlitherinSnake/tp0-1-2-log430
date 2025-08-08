package com.log430.tp7.presentation.api;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.log430.tp7.application.service.EventDrivenFulfillmentService;
import com.log430.tp7.presentation.api.dto.DeliveryRequest;
import com.log430.tp7.presentation.api.dto.FulfillmentRequest;
import com.log430.tp7.presentation.api.dto.FulfillmentResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * REST controller for order fulfillment operations.
 * Handles order fulfillment and delivery with event publishing.
 */
@RestController
@RequestMapping("/api/fulfillment")
@CrossOrigin(origins = "*")
@Tag(name = "Fulfillment", description = "Order fulfillment and delivery operations")
public class FulfillmentController {
    
    private static final Logger log = LoggerFactory.getLogger(FulfillmentController.class);
    
    private final EventDrivenFulfillmentService fulfillmentService;
    
    public FulfillmentController(EventDrivenFulfillmentService fulfillmentService) {
        this.fulfillmentService = fulfillmentService;
    }
    
    @PostMapping("/orders/{orderId}/fulfill")
    @Operation(summary = "Fulfill an order", description = "Mark an order as fulfilled and publish OrderFulfilled event")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Order fulfilled successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request or order cannot be fulfilled"),
        @ApiResponse(responseCode = "404", description = "Order not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<FulfillmentResponse> fulfillOrder(
            @Parameter(description = "Order ID", required = true) @PathVariable String orderId,
            @Valid @RequestBody FulfillmentRequest request) {
        
        log.info("Received fulfillment request for order: {}", orderId);
        
        try {
            LocalDateTime estimatedDeliveryTime = request.estimatedDeliveryTime() != null ? 
                request.estimatedDeliveryTime() : LocalDateTime.now().plusHours(2);
            
            boolean success = fulfillmentService.fulfillOrder(
                orderId, 
                request.correlationId(), 
                estimatedDeliveryTime
            );
            
            if (success) {
                return ResponseEntity.ok(FulfillmentResponse.success(
                    orderId, "Order fulfilled successfully", "FULFILLED"
                ));
            } else {
                return ResponseEntity.badRequest().body(FulfillmentResponse.failure(
                    orderId, "Failed to fulfill order", "FULFILLMENT_FAILED"
                ));
            }
            
        } catch (Exception e) {
            log.error("Error fulfilling order {}: {}", orderId, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(FulfillmentResponse.failure(
                orderId, "Internal error during fulfillment", "INTERNAL_ERROR"
            ));
        }
    }
    
    @PostMapping("/orders/{orderId}/deliver")
    @Operation(summary = "Deliver an order", description = "Mark an order as delivered and publish OrderDelivered event")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Order delivered successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request or order cannot be delivered"),
        @ApiResponse(responseCode = "404", description = "Order not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<FulfillmentResponse> deliverOrder(
            @Parameter(description = "Order ID", required = true) @PathVariable String orderId,
            @Valid @RequestBody DeliveryRequest request) {
        
        log.info("Received delivery request for order: {}", orderId);
        
        try {
            boolean success = fulfillmentService.deliverOrder(
                orderId,
                request.deliveryAddress(),
                request.deliveryMethod(),
                request.deliveryConfirmation(),
                request.correlationId()
            );
            
            if (success) {
                return ResponseEntity.ok(FulfillmentResponse.success(
                    orderId, "Order delivered successfully", "DELIVERED"
                ));
            } else {
                return ResponseEntity.badRequest().body(FulfillmentResponse.failure(
                    orderId, "Failed to deliver order", "DELIVERY_FAILED"
                ));
            }
            
        } catch (Exception e) {
            log.error("Error delivering order {}: {}", orderId, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(FulfillmentResponse.failure(
                orderId, "Internal error during delivery", "INTERNAL_ERROR"
            ));
        }
    }
    
    @GetMapping("/orders/{orderId}/status")
    @Operation(summary = "Get fulfillment status", description = "Get the current fulfillment status of an order")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Status retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Order not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<FulfillmentResponse> getFulfillmentStatus(
            @Parameter(description = "Order ID", required = true) @PathVariable String orderId) {
        
        log.info("Getting fulfillment status for order: {}", orderId);
        
        try {
            String status = fulfillmentService.getFulfillmentStatus(orderId);
            
            if ("NOT_FOUND".equals(status)) {
                return ResponseEntity.notFound().build();
            }
            
            if ("ERROR".equals(status)) {
                return ResponseEntity.internalServerError().body(FulfillmentResponse.failure(
                    orderId, "Error retrieving status", "INTERNAL_ERROR"
                ));
            }
            
            return ResponseEntity.ok(FulfillmentResponse.success(
                orderId, "Status retrieved successfully", status
            ));
            
        } catch (Exception e) {
            log.error("Error getting fulfillment status for order {}: {}", orderId, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(FulfillmentResponse.failure(
                orderId, "Internal error retrieving status", "INTERNAL_ERROR"
            ));
        }
    }
    
    @GetMapping("/orders/{orderId}/ready-for-fulfillment")
    @Operation(summary = "Check if order is ready for fulfillment", description = "Check if an order can be fulfilled")
    public ResponseEntity<Boolean> isOrderReadyForFulfillment(
            @Parameter(description = "Order ID", required = true) @PathVariable String orderId) {
        
        try {
            boolean ready = fulfillmentService.isOrderReadyForFulfillment(orderId);
            return ResponseEntity.ok(ready);
        } catch (Exception e) {
            log.error("Error checking fulfillment readiness for order {}: {}", orderId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/orders/{orderId}/ready-for-delivery")
    @Operation(summary = "Check if order is ready for delivery", description = "Check if an order can be delivered")
    public ResponseEntity<Boolean> isOrderReadyForDelivery(
            @Parameter(description = "Order ID", required = true) @PathVariable String orderId) {
        
        try {
            boolean ready = fulfillmentService.isOrderReadyForDelivery(orderId);
            return ResponseEntity.ok(ready);
        } catch (Exception e) {
            log.error("Error checking delivery readiness for order {}: {}", orderId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}