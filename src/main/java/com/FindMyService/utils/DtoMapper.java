package com.FindMyService.utils;

import com.FindMyService.model.ServiceCatalog;
import com.FindMyService.model.User;
import com.FindMyService.model.Provider;
import com.FindMyService.model.dto.ProviderDto;
import com.FindMyService.model.dto.ServiceCatalogDto;
import com.FindMyService.model.dto.UserDto;

public final class DtoMapper {

    private DtoMapper() {}

    public static UserDto toDto(User user) {
        if (user == null) return null;
        return UserDto.builder()
                .userId(user.getUserId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
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
                .phone(provider.getPhone())
                .addressLine1(provider.getAddressLine1())
                .addressLine2(provider.getAddressLine2())
                .state(provider.getState())
                .city(provider.getCity())
                .zipCode(provider.getZipCode())
                .createdAt(provider.getCreatedAt())
                .profilePictureUrl(provider.getProfilePictureUrl())
                .imageUrls(provider.getImageUrls())
                .avgRating(provider.getAvgRating())
                .totalRatings(provider.getTotalRatings())
                .build();
    }

    public static ServiceCatalogDto toDto(ServiceCatalog service) {
        if (service == null) return null;
        return ServiceCatalogDto.builder()
                .serviceId(service.getServiceId())
                .providerId(service.getProviderId() != null ? service.getProviderId().getProviderId() : null)
                .providerName(service.getProviderId() != null ? service.getProviderId().getProviderName() : null)
                .serviceName(service.getServiceName())
                .description(service.getDescription())
                .cost(service.getCost())
                .location(service.getLocation())
                .availability(service.getAvailability())
                .warrantyPeriodMonths(service.getWarrantyPeriodMonths())
                .imageUrl(service.getImageUrl())
                .createdAt(service.getCreatedAt())
                .updatedAt(service.getUpdatedAt())
                .active(service.isActive())
                .avgRating(service.getAvgRating())
                .totalRatings(service.getTotalRatings())
                .build();
    }

    public static ServiceCatalog toEntity(ServiceCatalogDto dto, Provider provider) {
        return ServiceCatalog.builder()
                .providerId(provider)
                .serviceName(dto.getServiceName())
                .description(dto.getDescription())
                .cost(dto.getCost())
                .location(dto.getLocation())
                .availability(dto.getAvailability())
                .warrantyPeriodMonths(dto.getWarrantyPeriodMonths())
                .imageUrl(dto.getImageUrl())
                .active(dto.getActive() != null ? dto.getActive() : true)
                .build();
    }
}
