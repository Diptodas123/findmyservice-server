package com.FindMyService.service;

import com.FindMyService.model.Provider;
import com.FindMyService.model.dto.ProviderDto;
import com.FindMyService.repository.ProviderRepository;
import com.FindMyService.utils.DtoMapper;
import com.FindMyService.utils.ResponseBuilder;
import com.FindMyService.utils.OwnerCheck;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class ProviderService {
    @Autowired
    private PasswordEncoder passwordEncoder;

    private final ProviderRepository providerRepository;

    public ProviderService(ProviderRepository providerRepository, OwnerCheck ownerCheck) {
        this.providerRepository = providerRepository;
    }

    public List<Provider> getAllProviders() {
        return providerRepository.findAll();
    }

    public Optional<Provider> getProviderById(Long providerId) {
        return providerRepository.findById(providerId);
    }

    @Transactional
    public ResponseEntity<?> createProvider(Provider provider) {
        if (provider.getEmail() == null || provider.getEmail().isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Email is required"));
        }

        if (provider.getPassword() == null || provider.getPassword().isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Password is required"));
        }

        try {
            provider.setPassword(passwordEncoder.encode(provider.getPassword()));
            Provider created = providerRepository.save(provider);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResponseBuilder.serverError("Failed to create provider: " + e.getMessage()));
        }
    }

    @Transactional
    public ProviderDto updateProvider(Long providerId, ProviderDto providerDto) {
        Provider existingProvider = providerRepository.findById(providerId)
                .orElseThrow(() -> new IllegalArgumentException("Provider not found with id: " + providerId));

        updateIfNotNull(providerDto.getProviderName(), existingProvider::setProviderName);
        updateIfNotNull(providerDto.getEmail(), existingProvider::setEmail);
        updateIfNotNull(providerDto.getPhone(), existingProvider::setPhone);
        updateIfNotNull(providerDto.getAddressLine1(), existingProvider::setAddressLine1);
        updateIfNotNull(providerDto.getAddressLine2(), existingProvider::setAddressLine2);
        updateIfNotNull(providerDto.getCity(), existingProvider::setCity);
        updateIfNotNull(providerDto.getState(), existingProvider::setState);
        updateIfNotNull(providerDto.getZipCode(), existingProvider::setZipCode);
        updateIfNotNull(providerDto.getProfilePictureUrl(), existingProvider::setProfilePictureUrl);

        if (providerDto.getPassword() != null && !providerDto.getPassword().isEmpty()) {
            if (providerDto.getCurrentPassword() == null || providerDto.getCurrentPassword().isEmpty()) {
                throw new IllegalArgumentException("Current password is required to update password");
            }

            if (!passwordEncoder.matches(providerDto.getCurrentPassword(), existingProvider.getPassword())) {
                throw new IllegalArgumentException("Current password is incorrect");
            }

            existingProvider.setPassword(passwordEncoder.encode(providerDto.getPassword()));
        }

        Provider updated = providerRepository.save(existingProvider);
        return DtoMapper.toDto(updated);
    }

    @Transactional
    public void deleteProvider(Long providerId) {
        Provider provider = providerRepository.findById(providerId)
                .orElseThrow(() -> new IllegalArgumentException("Provider not found with id: " + providerId));
        providerRepository.delete(provider);
    }

    private <T> void updateIfNotNull(T value, java.util.function.Consumer<T> setter) {
        if (value != null) {
            setter.accept(value);
        }
    }
}
