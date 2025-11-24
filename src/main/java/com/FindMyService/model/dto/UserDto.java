package com.FindMyService.model.dto;

import com.FindMyService.model.enums.Role;
import lombok.*;

import java.time.Instant;

@Data
@Builder
public class UserDto {

    private Long userId;
    private String name;
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
}
