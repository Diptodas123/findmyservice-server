package com.FindMyService.controller;

import com.FindMyService.model.dto.ServiceCatalogDto;
import com.FindMyService.service.ServiceCatalogService;
import com.FindMyService.utils.ResponseBuilder;
import com.FindMyService.utils.OwnerCheck;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RequestMapping("/api/v1/services")
@RestController
@RequiredArgsConstructor
public class ServiceCatalogController {

    private final ServiceCatalogService serviceCatalogService;
    private final OwnerCheck ownerCheck;

    @GetMapping
    public ResponseEntity<List<ServiceCatalogDto>> getAllServices() {
        return ResponseEntity.ok(serviceCatalogService.getAllServices());
    }

    @GetMapping("/{serviceId}")
    public ResponseEntity<ServiceCatalogDto> getService(@PathVariable Long serviceId) {
        return serviceCatalogService.getServiceById(serviceId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @GetMapping("/provider/{providerId}")
    public ResponseEntity<?> getServicesByProvider(@PathVariable Long providerId) {
        return ResponseEntity.ok(serviceCatalogService.getServicesByProvider(providerId));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('PROVIDER')")
    public ResponseEntity<?> createService(@RequestBody ServiceCatalogDto serviceDto) {
        ownerCheck.verifyOwner(serviceDto.getProviderId());
        return ResponseEntity.status(HttpStatus.CREATED).body(serviceCatalogService.createService(serviceDto));
    }

    @PatchMapping("/{serviceId}")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('PROVIDER')")
    public ResponseEntity<?> updateService(@PathVariable Long serviceId, @RequestBody ServiceCatalogDto serviceDto) {
        ServiceCatalogDto existing = serviceCatalogService.getServiceById(serviceId)
                .orElseThrow(() -> new IllegalArgumentException("Service not found with id: " + serviceId));
        ownerCheck.verifyOwner(existing.getProviderId());
        return ResponseEntity.ok(serviceCatalogService.updateService(serviceId, serviceDto));
    }

    @DeleteMapping("/{serviceId}")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('PROVIDER')")
    public ResponseEntity<?> deleteService(@PathVariable Long serviceId) {
        ServiceCatalogDto service = serviceCatalogService.getServiceById(serviceId)
                .orElseThrow(() -> new IllegalArgumentException("Service not found with id: " + serviceId));
        ownerCheck.verifyOwner(service.getProviderId());
        serviceCatalogService.deleteService(serviceId);
        return ResponseEntity.ok(ResponseBuilder.build(HttpStatus.OK, "Service deleted successfully"));
    }
}
