package com.FindMyService.controller;

import com.FindMyService.model.ServiceCatalog;
import com.FindMyService.service.ServiceCatalogService;
import com.FindMyService.utils.OwnerCheck;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RequestMapping("/api/v1/services")
@RestController
public class ServiceCatalogController {

    private final ServiceCatalogService serviceCatalogService;
    private final OwnerCheck ownerCheck;

    public ServiceCatalogController(ServiceCatalogService serviceCatalogService, OwnerCheck ownerCheck) {
        this.ownerCheck = ownerCheck;
        this.serviceCatalogService = serviceCatalogService;
    }

    @GetMapping
    public ResponseEntity<List<ServiceCatalog>> getAllServices() {
        return ResponseEntity.ok(serviceCatalogService.getAllServices());
    }

    @GetMapping("/{serviceId}")
    public ResponseEntity<ServiceCatalog> getService(@PathVariable Long serviceId) {
        return serviceCatalogService.getServiceById(serviceId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('PROVIDER')")
    public ResponseEntity<?> createService(@RequestBody ServiceCatalog service) {
        try {
            ownerCheck.verifyOwner(service.getProviderId().getProviderId());
        } catch (AccessDeniedException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return serviceCatalogService.createService(service);
    }

    @PutMapping("/{serviceId}")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('PROVIDER')")
    public ResponseEntity<?> updateService(@PathVariable Long serviceId, @RequestBody ServiceCatalog service) {
        try {
            ownerCheck.verifyOwner(service.getProviderId().getProviderId());
        } catch (AccessDeniedException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return serviceCatalogService.updateService(serviceId, service);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('PROVIDER')")
    public ResponseEntity<Void> deleteService(@PathVariable Long serviceId) {
        ServiceCatalog service = serviceCatalogService.getServiceById(serviceId)
                .orElseThrow(() -> new RuntimeException("Service not found"));
        try {
            ownerCheck.verifyOwner(service.getProviderId().getProviderId());
        } catch (AccessDeniedException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        boolean serviceToDelete = serviceCatalogService.deleteService(serviceId);
        if (serviceToDelete) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
}
