package com.FindMyService.controller;

import com.FindMyService.model.Order;
import com.FindMyService.model.dto.OrderDto;
import com.FindMyService.service.OrderService;
import com.FindMyService.utils.ResponseBuilder;
import com.FindMyService.utils.OwnerCheck;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RequestMapping("/api/v1/orders")
@RestController
public class OrderController {

    private final OrderService orderService;
    private final OwnerCheck ownerCheck;

    public OrderController(OrderService orderService, OwnerCheck ownerCheck) {
        this.orderService = orderService;
        this.ownerCheck = ownerCheck;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<List<Order>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<?> getOrderById(@PathVariable Long orderId) {
        return orderService.getOrderById(orderId)
                .map(order -> ResponseEntity.ok((Object) order))
                .orElseGet(() -> ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(ResponseBuilder.build(HttpStatus.NOT_FOUND, "Order not found")));
    }

    @PostMapping("/checkout")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('USER')")
    public ResponseEntity<?> checkout(@RequestBody java.util.List<OrderDto> orderDtos) {
        try {
            if (orderDtos == null || orderDtos.isEmpty()) {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(ResponseBuilder.badRequest("Order list cannot be empty"));
            }

            Long userId = orderDtos.getFirst().getUserId();
            ownerCheck.verifyOwner(userId);

            boolean allSameUser = orderDtos.stream().allMatch(dto -> dto.getUserId().equals(userId));
            if (!allSameUser) {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(ResponseBuilder.badRequest("All orders must belong to the same user"));
            }

            java.util.List<OrderDto> createdOrders = orderService.createOrdersBatch(orderDtos);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdOrders);
        } catch (AccessDeniedException ex) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(ResponseBuilder.forbidden("You are not authorized to create these orders"));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ResponseBuilder.badRequest(ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResponseBuilder.internalServerError("Failed to create orders: " + ex.getMessage()));
        }
    }

    @DeleteMapping("/{orderId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> deleteOrder(@PathVariable Long orderId) {
        boolean orderToDelete = orderService.deleteOrder(orderId);
        if (orderToDelete) {
            return ResponseEntity.ok(ResponseBuilder.ok("Order deleted successfully"));
        }
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ResponseBuilder.build(HttpStatus.NOT_FOUND, "Order not found with id: " + orderId));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('USER')")
    public ResponseEntity<?> getOrdersByUser(@PathVariable Long userId) {
        try {
            ownerCheck.verifyOwner(userId);
        } catch (AccessDeniedException ex) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(ResponseBuilder.forbidden("You are not authorized to access these orders"));
        }
        return orderService.getOrdersByUser(userId);
    }

    @GetMapping("/provider/{providerId}")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('PROVIDER')")
    public ResponseEntity<?> getOrdersByProvider(@PathVariable Long providerId) {
        try {
            ownerCheck.verifyOwner(providerId);
        } catch (AccessDeniedException ex) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(ResponseBuilder.forbidden("You are not authorized to access these orders"));
        }
        return orderService.getOrdersByProvider(providerId);
    }

    @PatchMapping("/{orderId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> updateOrder(@PathVariable Long orderId, @RequestBody OrderDto orderDto) {
        try {
            OrderDto updatedOrder = orderService.updateOrder(orderId, orderDto);
            return ResponseEntity.ok(updatedOrder);
        } catch (AccessDeniedException ex) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(ResponseBuilder.forbidden(ex.getMessage()));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ResponseBuilder.badRequest(ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResponseBuilder.internalServerError("Failed to update order: " + ex.getMessage()));
        }
    }
}
