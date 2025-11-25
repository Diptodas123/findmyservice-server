package com.FindMyService.controller;

import com.FindMyService.model.Provider;
import com.FindMyService.model.dto.ProviderDto;
import com.FindMyService.service.ProviderService;
import com.FindMyService.utils.DtoMapper;
import com.FindMyService.utils.OwnerCheck;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequestMapping("/api/v1/providers")
@RestController
public class ProviderController {

    private final ProviderService providerService;
    private final OwnerCheck ownerCheck;

    public ProviderController(ProviderService providerService, OwnerCheck ownerCheck) {
        this.providerService = providerService;
        this.ownerCheck = ownerCheck;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<List<ProviderDto>> getAllProviders() {
        List<ProviderDto> dtos = providerService.getAllProviders()
                .stream()
                .map(DtoMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{providerId}")
    public ResponseEntity<ProviderDto> getProvider(@PathVariable Long providerId) {
        return providerService.getProviderById(providerId)
                .map(DtoMapper::toDto)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Provider> createProvider(@RequestBody Provider provider) {
        Provider created = providerService.createProvider(provider);
        if (created == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{providerId}")
    @PreAuthorize("hasAuthority('PROVIDER') or hasAuthority('ADMIN')")
    public ResponseEntity<Provider> updateProvider(@PathVariable Long providerId, @RequestBody Provider provider) {
        try {
            ownerCheck.verifyOwner(providerId);
        } catch (AccessDeniedException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return providerService.updateProvider(providerId, provider)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @DeleteMapping("/{providerId}")
    @PreAuthorize("hasAuthority('PROVIDER') or hasAuthority('ADMIN')")
    public ResponseEntity<?> deleteProvider(@PathVariable Long providerId) {
        try {
            ownerCheck.verifyOwner(providerId);
        } catch (AccessDeniedException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        boolean deleted = providerService.deleteProvider(providerId);
        if (deleted) {
            String msg = String.format("Provider with id %d deleted successfully", providerId);
            return ResponseEntity.ok(Map.of("message", msg));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
}
