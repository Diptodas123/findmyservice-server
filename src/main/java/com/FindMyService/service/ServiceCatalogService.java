package com.FindMyService.service;

import com.FindMyService.model.ServiceCatalog;
import com.FindMyService.repository.ServiceCatalogRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class ServiceCatalogService {

    private final ServiceCatalogRepository serviceCatalogRepository;

    public ServiceCatalogService(ServiceCatalogRepository serviceCatalogRepository) {
        this.serviceCatalogRepository = serviceCatalogRepository;
    }

    public List<ServiceCatalog> getAllServices() {
        return serviceCatalogRepository.findAll();
    }

    public Optional<ServiceCatalog> getServiceById(Long serviceId) {
        return serviceCatalogRepository.findById(serviceId);
    }

    public ServiceCatalog createService(ServiceCatalog service) {
        return serviceCatalogRepository.save(service);
    }

    public Optional<ServiceCatalog> updateService(Long serviceId, ServiceCatalog service) {
        ServiceCatalog existingService = serviceCatalogRepository.findById(serviceId).orElse(null);
        if (existingService == null) {
            return Optional.empty();
        }
        service.setServiceId(serviceId);
        ServiceCatalog updatedService = serviceCatalogRepository.save(service);
        return Optional.of(updatedService);
    }

    public boolean deleteService(Long serviceId) {
        return serviceCatalogRepository.findById(serviceId).map(service -> {
            serviceCatalogRepository.delete(service);
            return true;
        }).orElse(false);
    }
}
