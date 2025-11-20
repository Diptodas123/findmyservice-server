package com.FindMyService.controller;

import com.FindMyService.model.Service;
import com.FindMyService.service.ServiceCatalogService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/api/v1/services")
@RestController
public class ServiceController {

    private final ServiceCatalogService serviceCatalogService;

    public ServiceController(ServiceCatalogService serviceCatalogService) {
        this.serviceCatalogService = serviceCatalogService;
    }

    @GetMapping
    public ResponseEntity<List<Service>> getAllServices() {
        return ResponseEntity.ok(serviceCatalogService.getAllServices());
    }

    @GetMapping("/{serviceId}")
    public ResponseEntity<Service> getService(@PathVariable String serviceId) {
        return serviceCatalogService.getServiceById(serviceId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PostMapping
    public ResponseEntity<Service> createService(@RequestBody Service service) {
        Service newService = serviceCatalogService.createService(service);
        if(newService == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(newService);
    }

    @PutMapping("/{serviceId}")
    public ResponseEntity<Service> updateService(@PathVariable String serviceId, @RequestBody Service service) {
        return serviceCatalogService.updateService(serviceId, service)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteService(@PathVariable String serviceId) {
        boolean serviceToDelete = serviceCatalogService.deleteService(serviceId);
        if (serviceToDelete) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
}
