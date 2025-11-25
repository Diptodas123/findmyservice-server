package com.FindMyService.service;

import com.FindMyService.model.Provider;
import com.FindMyService.model.ServiceCatalog;
import com.FindMyService.repository.ProviderRepository;
import com.FindMyService.repository.ServiceCatalogRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
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

    public ResponseEntity<?> createService(ServiceCatalog service) {
        Optional<Provider> provider = providerRepository.findById(service.getProviderId().getProviderId());
        if (provider.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Provider from payload not found"));
        }

        ServiceCatalog saved = serviceCatalogRepository.save(service);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    public ResponseEntity<?> updateService(Long serviceId, ServiceCatalog service) {
        Optional<Provider> provider = providerRepository.findById(service.getProviderId().getProviderId());
        if (provider.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Provider from payload not found"));
        }

        Optional<ServiceCatalog> existingService = serviceCatalogRepository.findById(serviceId);
        if (existingService.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Service from payload not found"));
        }

        ServiceCatalog updatedService = serviceCatalogRepository.save(service);
        return ResponseEntity.status(HttpStatus.OK).body(updatedService);
    }

    public boolean deleteService(Long serviceId) {
        return serviceCatalogRepository.findById(serviceId).map(service -> {
            serviceCatalogRepository.delete(service);
            return true;
        }).orElse(false);
    }
}
