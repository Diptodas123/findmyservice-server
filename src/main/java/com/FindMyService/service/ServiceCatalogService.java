package com.FindMyService.service;

import com.FindMyService.model.Provider;
import com.FindMyService.model.ServiceCatalog;
import com.FindMyService.model.dto.ServiceCatalogDto;
import com.FindMyService.repository.ProviderRepository;
import com.FindMyService.repository.ServiceCatalogRepository;
import com.FindMyService.utils.DtoMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class ServiceCatalogService {

    private final ServiceCatalogRepository serviceCatalogRepository;
    private final ProviderRepository providerRepository;

    public ServiceCatalogService(ServiceCatalogRepository serviceCatalogRepository,
                                 ProviderRepository providerRepository) {
        this.serviceCatalogRepository = serviceCatalogRepository;
        this.providerRepository = providerRepository;
    }

    public List<ServiceCatalog> getAllServices() {
        return serviceCatalogRepository.findAll();
    }

    public Optional<ServiceCatalog> getServiceById(Long serviceId) {
        return serviceCatalogRepository.findById(serviceId);
    }

    public List<ServiceCatalog> getServicesByProvider(Long providerId) {
        if (providerId == null || providerId <= 0) {
            return List.of();
        }
        if (!providerRepository.existsById(providerId)) {
            return List.of();
        }
        return serviceCatalogRepository.findByProviderId_ProviderId(providerId);
    }

    @Transactional
    public ServiceCatalogDto createService(ServiceCatalogDto serviceDto) {
        Provider provider = providerRepository.findById(serviceDto.getProviderId())
                .orElseThrow(() -> new IllegalArgumentException("Provider from payload not found"));

        ServiceCatalog serviceEntity = DtoMapper.toEntity(serviceDto, provider);
        ServiceCatalog saved = serviceCatalogRepository.save(serviceEntity);
        return DtoMapper.toDto(saved);
    }

    @Transactional
    public ServiceCatalogDto updateService(Long serviceId, ServiceCatalogDto serviceDto) {
        ServiceCatalog existingService = serviceCatalogRepository.findById(serviceId)
                .orElseThrow(() -> new IllegalArgumentException("Service not found with id: " + serviceId));

        if (serviceDto.getProviderId() != null) {
            Provider provider = providerRepository.findById(serviceDto.getProviderId())
                    .orElseThrow(() -> new IllegalArgumentException("Provider from payload not found"));
            existingService.setProviderId(provider);
        }

        updateIfNotNull(serviceDto.getServiceName(), existingService::setServiceName);
        updateIfNotNull(serviceDto.getDescription(), existingService::setDescription);
        updateIfNotNull(serviceDto.getCost(), existingService::setCost);
        updateIfNotNull(serviceDto.getLocation(), existingService::setLocation);
        updateIfNotNull(serviceDto.getAvailability(), existingService::setAvailability);
        updateIfNotNull(serviceDto.getWarrantyPeriodMonths(), existingService::setWarrantyPeriodMonths);
        updateIfNotNull(serviceDto.getImageUrl(), existingService::setImageUrl);
        updateIfNotNull(serviceDto.getActive(), existingService::setActive);

        ServiceCatalog updatedService = serviceCatalogRepository.save(existingService);
        return DtoMapper.toDto(updatedService);
    }

    @Transactional
    public void deleteService(Long serviceId) {
        ServiceCatalog service = serviceCatalogRepository.findById(serviceId)
                .orElseThrow(() -> new IllegalArgumentException("Service not found with id: " + serviceId));
        serviceCatalogRepository.delete(service);
    }

    private <T> void updateIfNotNull(T value, java.util.function.Consumer<T> setter) {
        if (value != null) {
            setter.accept(value);
        }
    }
}
