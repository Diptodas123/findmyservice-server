package com.FindMyService.model;

import com.FindMyService.model.enums.Availability;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import lombok.*;
import org.springframework.lang.Nullable;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "services",
        uniqueConstraints = @UniqueConstraint(name = "uk_service_provider_name", columnNames = {"provider_id","service_name"}))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Service {
    @Id
    @Column(length = 64)
    private String serviceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_id", nullable = false)
    private Provider providerId;

    @Column(name = "service_name", nullable = false, length = 160)
    private String serviceName;

    @Lob
    private String description;

    @Column(precision = 7, scale = 2)
    @DecimalMin(value = "0.0", inclusive = false, message = "Cost must be greater than 0")
    private BigDecimal cost;

    @Column(length = 120)
    private String location;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private Availability availability;

    @Nullable
    private Integer warrantyPeriodMonths;
    private String imageUrl;
    private Instant createdAt;
    private Instant updatedAt;
    private boolean active;
}
