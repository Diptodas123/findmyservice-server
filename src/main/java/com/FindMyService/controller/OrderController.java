package com.FindMyService.controller;

import com.FindMyService.model.dto.OrderDto;
import com.FindMyService.service.OrderService;
import com.FindMyService.utils.ResponseBuilder;
import com.FindMyService.utils.OwnerCheck;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RequestMapping("/api/v1/orders")
@RestController
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final OwnerCheck ownerCheck;

    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<List<OrderDto>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<?> getOrderById(@PathVariable Long orderId) {
        return orderService.getOrderById(orderId)
                .map(dto -> ResponseEntity.ok((Object) dto))
                .orElseGet(() -> ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(ResponseBuilder.build(HttpStatus.NOT_FOUND, "Order not found")));
    }

    @PostMapping("/checkout")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('USER')")
    public ResponseEntity<?> checkout(@RequestBody List<OrderDto> orderDtos) {
        if (orderDtos == null || orderDtos.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Order list cannot be empty"));
        }
        Long userId = orderDtos.getFirst().getUserId();
        ownerCheck.verifyOwner(userId);
        if (orderDtos.stream().anyMatch(dto -> !dto.getUserId().equals(userId))) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ResponseBuilder.build(HttpStatus.BAD_REQUEST, "All orders must belong to the same user"));
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.createOrdersBatch(orderDtos));
    }

    @DeleteMapping("/{orderId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> deleteOrder(@PathVariable Long orderId) {
        if (orderService.deleteOrder(orderId)) {
            return ResponseEntity.ok(ResponseBuilder.build(HttpStatus.OK, "Order deleted successfully"));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ResponseBuilder.build(HttpStatus.NOT_FOUND, "Order not found with id: " + orderId));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('USER')")
    public ResponseEntity<?> getOrdersByUser(@PathVariable Long userId) {
        ownerCheck.verifyOwner(userId);
        return orderService.getOrdersByUser(userId);
    }

    @GetMapping("/provider/{providerId}")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('PROVIDER')")
    public ResponseEntity<?> getOrdersByProvider(@PathVariable Long providerId) {
        ownerCheck.verifyOwner(providerId);
        return orderService.getOrdersByProvider(providerId);
    }

    @PatchMapping("/{orderId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> updateOrder(@PathVariable Long orderId, @RequestBody OrderDto orderDto) {
        return ResponseEntity.ok(orderService.updateOrder(orderId, orderDto));
    }
}
