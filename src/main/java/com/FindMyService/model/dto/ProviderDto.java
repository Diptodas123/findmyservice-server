package com.FindMyService.model.dto;

import com.FindMyService.model.enums.Role;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@Builder
public class ProviderDto{
    private Long providerId;
    private String providerName;
    private String email;
    private Role role;
    private String phone;
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;
    private String zipCode;
    private Instant createdAt;
    private String profilePictureUrl;
    private List<String> imageUrls;
    private BigDecimal avgRating;
    private int totalRatings;
}
