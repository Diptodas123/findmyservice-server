package com.FindMyService.model;

import com.FindMyService.model.enums.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "users", uniqueConstraints = @UniqueConstraint(name = "uk_users_email", columnNames = "email"))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @Column(length = 64)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @NonNull
    private String name;

    @Column(nullable = false, unique = true)
    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    @Size(max = 100)
    private String email;

    @NotBlank(message = "Password is required")
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

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
    private Instant createdAt = Instant.now();
    private String profilePictureUrl;
}
