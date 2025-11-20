package com.FindMyService.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "feedback")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Feedback {
    @Id
    @Column(length = 64)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long feedbackId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id", nullable = false)
    private Long serviceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Long orderId;

    @Lob
    private String comment;

    private Instant createdAt;
}
