package com.FindMyService.controller;

import com.FindMyService.model.ServiceCatalog;
import com.FindMyService.model.dto.ServiceCatalogDto;
import com.FindMyService.service.ServiceCatalogService;
import com.FindMyService.utils.DtoMapper;
import com.FindMyService.utils.ResponseBuilder;
import com.FindMyService.utils.OwnerCheck;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    public ResponseEntity<List<ServiceCatalogDto>> getAllServices() {
        List<ServiceCatalog> services = serviceCatalogService.getAllServices();
        List<ServiceCatalogDto> serviceDtos = services.stream()
                .map(DtoMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(serviceDtos);
    }

    @GetMapping("/{serviceId}")
    public ResponseEntity<ServiceCatalogDto> getService(@PathVariable Long serviceId) {
        return serviceCatalogService.getServiceById(serviceId)
                .map(DtoMapper::toDto)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @GetMapping("/provider/{providerId}")
    public ResponseEntity<?> getServicesByProvider(@PathVariable Long providerId) {
        try {
            ownerCheck.verifyOwner(providerId);
            List<ServiceCatalog> services = serviceCatalogService.getServicesByProvider(providerId);
            List<ServiceCatalogDto> serviceDtos = services.stream()
                    .map(DtoMapper::toDto)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(serviceDtos);
        } catch (AccessDeniedException ex) {
            Map<String, Object> errorBody = ResponseBuilder.forbidden(
                    "You are not authorized to access services for this provider"
            );
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorBody);
        }
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('PROVIDER')")
    public ResponseEntity<?> createService(@RequestBody ServiceCatalogDto serviceDto) {
        try {
            ownerCheck.verifyOwner(serviceDto.getProviderId());
            ServiceCatalogDto createdService = serviceCatalogService.createService(serviceDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdService);
        } catch (AccessDeniedException ex) {
            Map<String, Object> errorBody = ResponseBuilder.forbidden(
                    "You are not authorized to create service for this provider"
            );
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorBody);
        } catch (IllegalArgumentException ex) {
            Map<String, Object> errorBody = ResponseBuilder.badRequest(ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorBody);
        } catch (Exception ex) {
            Map<String, Object> errorBody = ResponseBuilder.internalServerError(
                    "Failed to create service: " + ex.getMessage()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorBody);
        }
    }

    @PatchMapping("/{serviceId}")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('PROVIDER')")
    public ResponseEntity<?> updateService(@PathVariable Long serviceId, @RequestBody ServiceCatalogDto serviceDto) {
        try {
            ServiceCatalog existingService = serviceCatalogService.getServiceById(serviceId)
                    .orElseThrow(() -> new IllegalArgumentException("Service not found with id: " + serviceId));

            ownerCheck.verifyOwner(existingService.getProviderId().getProviderId());

            ServiceCatalogDto updatedService = serviceCatalogService.updateService(serviceId, serviceDto);
            return ResponseEntity.ok(updatedService);
        } catch (AccessDeniedException ex) {
            Map<String, Object> errorBody = ResponseBuilder.forbidden(
                    "You are not authorized to update this service"
            );
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorBody);
        } catch (IllegalArgumentException ex) {
            Map<String, Object> errorBody = ResponseBuilder.notFound(ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorBody);
        } catch (Exception ex) {
            Map<String, Object> errorBody = ResponseBuilder.internalServerError(
                    "Failed to update service: " + ex.getMessage()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorBody);
        }
    }

    @DeleteMapping("/{serviceId}")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('PROVIDER')")
    public ResponseEntity<?> deleteService(@PathVariable Long serviceId) {
        try {
            ServiceCatalog service = serviceCatalogService.getServiceById(serviceId)
                    .orElseThrow(() -> new IllegalArgumentException("Service not found with id: " + serviceId));

            ownerCheck.verifyOwner(service.getProviderId().getProviderId());
            serviceCatalogService.deleteService(serviceId);

            Map<String, Object> responseBody = ResponseBuilder.ok(
                    "Service deleted successfully"
            );
            return ResponseEntity.ok(responseBody);
        } catch (AccessDeniedException ex) {
            Map<String, Object> errorBody = ResponseBuilder.forbidden(
                    "You are not authorized to delete this service"
            );
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorBody);
        } catch (IllegalArgumentException ex) {
            Map<String, Object> errorBody = ResponseBuilder.notFound(ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorBody);
        } catch (Exception ex) {
            Map<String, Object> errorBody = ResponseBuilder.internalServerError(
                    "Failed to delete service: " + ex.getMessage()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorBody);
        }
    }
}
