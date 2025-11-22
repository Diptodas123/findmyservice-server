package com.FindMyService.controller;

import com.FindMyService.model.ServiceCatalog;
import com.FindMyService.service.ServiceCatalogService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RequestMapping("/api/v1/services")
@RestController
public class ServiceCatalogController {

    private final ServiceCatalogService serviceCatalogService;

    public ServiceCatalogController(ServiceCatalogService serviceCatalogService) {
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
    public ResponseEntity<ServiceCatalog> createService(@RequestBody ServiceCatalog service) {
        ServiceCatalog newService = serviceCatalogService.createService(service);
        if(newService == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(newService);
    }

    @PutMapping("/{serviceId}")
    public ResponseEntity<ServiceCatalog> updateService(@PathVariable Long serviceId, @RequestBody ServiceCatalog service) {
        return serviceCatalogService.updateService(serviceId, service)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteService(@PathVariable Long serviceId) {
        boolean serviceToDelete = serviceCatalogService.deleteService(serviceId);
        if (serviceToDelete) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
}
