package com.FindMyService.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "service_attributes")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceAttribute {
    @Id
    @Column(length = 64)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long attributeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id", nullable = false)
    private ServiceCatalog serviceId;

    @Column(length = 100, nullable = false)
    private String attributeName;

    @Column(length = 120, nullable = false)
    private String attributeValue;

    @Column(length = 60)
    @Pattern(
            regexp = "^(string|integer|float|boolean|date|enum)$",
            message = "valueType must be one of: string, integer, float, boolean, date, enum"
    )
    private String valueType;

    private Instant createdAt = Instant.now();
}
