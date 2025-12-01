package com.FindMyService.model.dto;

import com.FindMyService.model.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDto {
    private Long orderId;
    private Long userId;
    private Long providerId;
    private Long serviceId;
    private OrderStatus orderStatus;
    private BigDecimal totalCost;
    private Integer quantity;
    private Instant requestedDate;
    private Instant scheduledDate;
    private Instant createdAt;
    private Instant updatedAt;
}

