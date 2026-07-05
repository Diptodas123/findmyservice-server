package com.FindMyService.controller;

import com.FindMyService.model.Provider;
import com.FindMyService.model.dto.ProviderDto;
import com.FindMyService.service.ProviderService;
import com.FindMyService.utils.ResponseBuilder;
import com.FindMyService.utils.OwnerCheck;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RequestMapping("/api/v1/providers")
@RestController
@RequiredArgsConstructor
public class ProviderController {

    private final ProviderService providerService;
    private final OwnerCheck ownerCheck;

    @GetMapping
    public ResponseEntity<List<ProviderDto>> getAllProviders() {
        return ResponseEntity.ok(providerService.getAllProviders());
    }

    @GetMapping("/{providerId}")
    public ResponseEntity<?> getProvider(@PathVariable Long providerId) {
        return providerService.getProviderById(providerId)
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
        ownerCheck.verifyOwner(providerId);
        return ResponseEntity.ok(providerService.updateProvider(providerId, providerDto));
    }

    @DeleteMapping("/{providerId}")
    @PreAuthorize("hasAuthority('PROVIDER') or hasAuthority('ADMIN')")
    public ResponseEntity<?> deleteProvider(@PathVariable Long providerId) {
        ownerCheck.verifyOwner(providerId);
        providerService.deleteProvider(providerId);
        return ResponseEntity.ok(ResponseBuilder.build(HttpStatus.OK, "Provider deleted successfully"));
    }
}
