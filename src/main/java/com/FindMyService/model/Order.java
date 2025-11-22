package com.FindMyService.model;

import com.FindMyService.model.enums.OrderStatus;
import com.FindMyService.model.enums.PaymentMethod;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "orders")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    @Id
    @Column(length = 64)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id", nullable = false)
    private ServiceCatalog serviceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ordered_by_user_id", nullable = false)
    private User UserId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_id", nullable = false)
    private Provider providerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OrderStatus orderStatus;

    @NonNull
    private Integer quantityUnits;

    private LocalDate requestedDate;
    private LocalDate scheduledDate;

    @Column(precision = 8, scale = 2)
    @DecimalMin(value = "0.0", inclusive = false, message = "Cost must be greater than 0")
    private BigDecimal totalCost;

    @Column(length = 256)
    private String transactionId;

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    private Instant paymentDate;
    private Instant createdAt;
    private Instant updatedAt;
}
