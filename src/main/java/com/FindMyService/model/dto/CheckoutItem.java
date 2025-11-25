package com.FindMyService.model.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class CheckoutItem {
    private Long serviceId;
    private Integer quantity = 1;
    private LocalDate requestedDate;
    private LocalDate scheduledDate;
}
