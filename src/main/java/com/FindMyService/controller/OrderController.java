package com.FindMyService.controller;

import com.FindMyService.model.Order;
import com.FindMyService.model.enums.OrderStatus;
import com.FindMyService.service.OrderService;
import com.FindMyService.utils.OwnerCheck;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RequestMapping("/api/v1/orders")
@RestController
public class OrderController {

    private final OrderService orderService;
    private final OwnerCheck ownerCheck;
    public OrderController(OrderService orderService, OwnerCheck OwnerCheck) {
        this.orderService = orderService;
        this.ownerCheck = OwnerCheck;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<List<Order>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<Order> getOrderById(@PathVariable Long orderId) {
        return orderService.getOrderById(orderId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('USER')")
    public ResponseEntity<?> createOrder(@RequestBody Order order) {
        try {
            ownerCheck.verifyOwner(order.getUserId().getUserId());
        } catch (AccessDeniedException ex) {
            Map<String, Object> errorBody = Map.of(
                    "status", HttpStatus.FORBIDDEN.value(),
                    "error", "Forbidden",
                    "message", "You are not authorized to access these orders"
            );
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorBody);
        }
        return orderService.createOrder(order);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<String> deleteOrder(@PathVariable Long orderId) {
        boolean orderToDelete = orderService.deleteOrder(orderId);
        if (orderToDelete) {
            return ResponseEntity.ok("Order deleted successfully.");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Order not found with id: " + orderId);
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAuthority('ADMIN') or (hasAuthority('USER')")
    public ResponseEntity<?> getOrdersByUser(@PathVariable Long userId) {
        try {
            ownerCheck.verifyOwner(userId);
        } catch (AccessDeniedException ex) {
            Map<String, Object> errorBody = Map.of(
                    "status", HttpStatus.FORBIDDEN.value(),
                    "error", "Forbidden",
                    "message", "You are not authorized to access these orders"
            );
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorBody);
        }
        return ResponseEntity.ok(orderService.getOrdersByUser(userId));
    }

    @GetMapping("/provider/{providerId}")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('PROVIDER')")
    public ResponseEntity<?> getOrdersByProvider(@PathVariable Long providerId) {
        try {
            ownerCheck.verifyOwner(providerId);
        } catch (AccessDeniedException ex) {
            Map<String, Object> errorBody = Map.of(
                    "status", HttpStatus.FORBIDDEN.value(),
                    "error", "Forbidden",
                    "message", "You are not authorized to access these orders"
            );
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorBody);
        }
        return ResponseEntity.ok(orderService.getOrdersByProvider(providerId));
    }

    @PostMapping("/{orderId}/payment/initiate")
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<?> initiatePayment(@PathVariable Long orderId) {
        return orderService.initiatePayment(orderId);
    }

    @PostMapping("/{orderId}/payment/confirm")
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<?> confirmPayment(
            @PathVariable Long orderId,
            @RequestBody Map<String, String> payload) {
        return orderService.confirmPayment(orderId, payload.get("paymentIntentId"));
    }

    @PutMapping("/{orderId}/status")
    public ResponseEntity<?> updateOrderStatus(@PathVariable Long orderId, OrderStatus newStatus) {
        Order order = orderService.getOrderById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        try {
            ownerCheck.verifyOwner(order.getProviderId().getProviderId());
        } catch (AccessDeniedException ex) {
            Map<String, Object> errorBody = Map.of(
                    "status", HttpStatus.FORBIDDEN.value(),
                    "error", "Forbidden",
                    "message", "You are not authorized to access these orders"
            );
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorBody);
        }
        return orderService.updateOrderStatus(orderId, newStatus);
    }
}
