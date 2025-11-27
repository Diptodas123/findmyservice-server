package com.FindMyService.service;

import com.FindMyService.model.Provider;
import com.FindMyService.model.ServiceCatalog;
import com.FindMyService.repository.ProviderRepository;
import com.FindMyService.repository.ServiceCatalogRepository;
import com.FindMyService.utils.ErrorResponseBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
        if (serviceId == null || serviceId <= 0) {
            return Optional.empty();
        }
        return serviceCatalogRepository.findById(serviceId);
    }

    public List<ServiceCatalog> getServicesByProvider(Long providerId) {
        if (providerId == null || providerId <= 0) {
            return List.of();
        }
        if (!providerRepository.existsById(providerId)) {
            return List.of();
        }
        return serviceCatalogRepository.findByProviderId(providerId);
    }

    @Transactional
    public ResponseEntity<?> createService(ServiceCatalog service) {
        if (service == null) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ErrorResponseBuilder.build(HttpStatus.BAD_REQUEST, "Service data must be provided"));
        }

        if (service.getProviderId() == null || service.getProviderId().getProviderId() == null) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ErrorResponseBuilder.build(HttpStatus.BAD_REQUEST, "Provider id must be provided in payload"));
        }

        Long payloadProviderId = service.getProviderId().getProviderId();
        Optional<Provider> provider = providerRepository.findById(payloadProviderId);
        if (provider.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ErrorResponseBuilder.build(HttpStatus.BAD_REQUEST, "Provider from payload not found"));
        }

        try {
            service.setProviderId(provider.get());
            ServiceCatalog saved = serviceCatalogRepository.save(service);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponseBuilder.serverError("Failed to create service: " + e.getMessage()));
        }
    }

    @Transactional
    public ResponseEntity<?> updateService(Long serviceId, ServiceCatalog service) {
        if (serviceId == null || serviceId <= 0) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ErrorResponseBuilder.build(HttpStatus.BAD_REQUEST, "Invalid service id"));
        }

        Optional<ServiceCatalog> existingService = serviceCatalogRepository.findById(serviceId);
        if (existingService.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponseBuilder.build(HttpStatus.NOT_FOUND, "Service not found"));
        }

        if (service == null) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ErrorResponseBuilder.build(HttpStatus.BAD_REQUEST, "Service data must be provided"));
        }

        if (service.getProviderId() == null || service.getProviderId().getProviderId() == null) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ErrorResponseBuilder.build(HttpStatus.BAD_REQUEST, "Provider id must be provided in payload"));
        }

        Long payloadProviderId = service.getProviderId().getProviderId();
        Optional<Provider> provider = providerRepository.findById(payloadProviderId);
        if (provider.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ErrorResponseBuilder.build(HttpStatus.BAD_REQUEST, "Provider from payload not found"));
        }

        try {
            service.setServiceId(serviceId);
            service.setProviderId(provider.get());
            ServiceCatalog updatedService = serviceCatalogRepository.save(service);
            return ResponseEntity.ok(updatedService);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponseBuilder.serverError("Failed to update service: " + e.getMessage()));
        }
    }

    @Transactional
    public ResponseEntity<?> deleteService(Long serviceId) {
        if (serviceId == null || serviceId <= 0) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ErrorResponseBuilder.build(HttpStatus.BAD_REQUEST, "Invalid service id"));
        }

        Optional<ServiceCatalog> service = serviceCatalogRepository.findById(serviceId);
        if (service.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponseBuilder.build(HttpStatus.NOT_FOUND, "Service not found"));
        }

        try {
            serviceCatalogRepository.delete(service.get());
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponseBuilder.serverError("Failed to delete service: " + e.getMessage()));
        }
    }
}
