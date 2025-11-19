package com.FindMyService.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "providers")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Provider {
    @Id
    @Column(length = 64)
    private String providerId;

    @Column(nullable = false, length = 160)
    private String providerName;

    @Column(nullable = false, unique = true)
    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    @Size(max = 100)
    private String email;

    private String phone;

    @NonNull
    private String addressLine1;

    private String addressLine2;

    @NonNull
    private String city;

    @NonNull
    private String state;

    @NonNull
    private String zipCode;

    @ElementCollection
    @Column(name = "image_url")
    private List<String> imageUrls;

    private boolean verified;
    private Instant createdAt;

    @Column(precision = 2, scale = 1)  // Example: 4.5
    @DecimalMin("0.0")
    @DecimalMax("5.0")
    private BigDecimal avgRating;
}
