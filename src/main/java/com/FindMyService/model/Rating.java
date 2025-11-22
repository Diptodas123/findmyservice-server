package com.FindMyService.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "ratings")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Rating {
    @Id
    @Column(length = 64)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long ratingId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id", nullable = false)
    private ServiceCatalog serviceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order orderId;

    @Column(nullable = false)
    @Min(0)
    @Max(5)
    private Integer score;

    private Instant createdAt;
}
