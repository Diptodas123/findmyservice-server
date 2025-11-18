package com.FindMyService.model;

import jakarta.persistence.*;
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
    private String attributeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id", nullable = false)
    private Service serviceId;

    @Column(length = 100, nullable = false)
    private String attributeName;

    @Column(length = 120, nullable = false)
    private String attributeValue;

    @Column(length = 60)
    private String valueType;

    private Instant createdAt;
}
