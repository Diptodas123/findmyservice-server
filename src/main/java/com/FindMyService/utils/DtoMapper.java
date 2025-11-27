package com.FindMyService.utils;

import com.FindMyService.model.User;
import com.FindMyService.model.Provider;
import com.FindMyService.model.dto.ProviderDto;
import com.FindMyService.model.dto.UserDto;
import com.FindMyService.model.enums.Role;

public final class DtoMapper {

    private DtoMapper() {}

    public static UserDto toDto(User user) {
        if (user == null) return null;
        return UserDto.builder()
                .userId(user.getUserId())
                .name(user.getName())
                .email(user.getEmail())
                .role(Role.USER)
                .phone(user.getPhone())
                .addressLine1(user.getAddressLine1())
                .addressLine2(user.getAddressLine2())
                .state(user.getState())
                .city(user.getCity())
                .zipCode(user.getZipCode())
                .createdAt(user.getCreatedAt())
                .profilePictureUrl(user.getProfilePictureUrl())
                .build();
    }

    public static ProviderDto toDto(Provider provider) {
        if (provider == null) return null;
        return ProviderDto.builder()
                .providerId(provider.getProviderId())
                .providerName(provider.getProviderName())
                .email(provider.getEmail())
                .role(Role.PROVIDER)
                .phone(provider.getPhone())
                .addressLine1(provider.getAddressLine1())
                .addressLine2(provider.getAddressLine2())
                .state(provider.getState())
                .city(provider.getCity())
                .zipCode(provider.getZipCode())
                .createdAt(provider.getCreatedAt())
                .avgRating(provider.getAvgRating())
                .build();
    }
}
