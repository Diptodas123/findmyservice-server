package com.FindMyService.controller;

import com.FindMyService.model.Provider;
import com.FindMyService.model.dto.ProviderDto;
import com.FindMyService.service.ProviderService;
import com.FindMyService.utils.DtoMapper;
import com.FindMyService.utils.ResponseBuilder;
import com.FindMyService.utils.OwnerCheck;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
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
    public ResponseEntity<List<ProviderDto>> getAllProviders() {
        List<ProviderDto> dtos = providerService.getAllProviders()
                .stream()
                .map(DtoMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{providerId}")
    public ResponseEntity<?> getProvider(@PathVariable Long providerId) {
        return providerService.getProviderById(providerId)
                .map(DtoMapper::toDto)
                .map(dto -> ResponseEntity.ok((Object) dto))
                .orElseGet(() -> ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(ResponseBuilder.build(HttpStatus.NOT_FOUND, "Provider not found")));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> createProvider(@RequestBody Provider provider) {
        return providerService.createProvider(provider);
    }

    @PatchMapping("/{providerId}")
    @PreAuthorize("hasAuthority('PROVIDER') or hasAuthority('ADMIN')")
    public ResponseEntity<?> updateProvider(@PathVariable Long providerId, @RequestBody ProviderDto providerDto) {
        try {
            ownerCheck.verifyOwner(providerId);
            ProviderDto updatedProvider = providerService.updateProvider(providerId, providerDto);
            return ResponseEntity.ok(updatedProvider);
        } catch (AccessDeniedException ex) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(ResponseBuilder.forbidden("You are not authorized to update this provider"));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ResponseBuilder.notFound(ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResponseBuilder.internalServerError("Failed to update provider: " + ex.getMessage()));
        }
    }

    @DeleteMapping("/{providerId}")
    @PreAuthorize("hasAuthority('PROVIDER') or hasAuthority('ADMIN')")
    public ResponseEntity<?> deleteProvider(@PathVariable Long providerId) {
        try {
            ownerCheck.verifyOwner(providerId);
            providerService.deleteProvider(providerId);
            return ResponseEntity.ok(ResponseBuilder.ok("Provider deleted successfully"));
        } catch (AccessDeniedException ex) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(ResponseBuilder.forbidden("You are not authorized to delete this provider"));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ResponseBuilder.notFound(ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResponseBuilder.internalServerError("Failed to delete provider: " + ex.getMessage()));
        }
    }
}
