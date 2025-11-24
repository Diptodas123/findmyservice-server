package com.FindMyService.service;

import com.FindMyService.model.Provider;
import com.FindMyService.repository.ProviderRepository;
import com.FindMyService.utils.OwnerCheck;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class ProviderService {
    @Autowired
    private PasswordEncoder passwordEncoder;

    private final ProviderRepository providerRepository;
    private final OwnerCheck ownerCheck;

    public ProviderService(ProviderRepository providerRepository, OwnerCheck ownerCheck) {
        this.providerRepository = providerRepository;
        this.ownerCheck = ownerCheck;
    }

    public List<Provider> getAllProviders() {
        return providerRepository.findAll();
    }

    public Optional<Provider> getProviderById(Long providerId) {
        return providerRepository.findById(providerId);
    }

    public Provider createProvider(Provider provider) {
        provider.setPassword(passwordEncoder.encode(provider.getPassword()));
        return providerRepository.save(provider);
    }

    public Optional<Provider> updateProvider(Long providerId, Provider provider) {
        ownerCheck.verifyOwnerOrAdmin(providerId);

        Provider existingProvider = providerRepository.findById(providerId).orElse(null);
        if (existingProvider == null) {
            return Optional.empty();
        }
        provider.setProviderId(providerId);
        if (provider.getPassword() != null) {
            provider.setPassword(passwordEncoder.encode(provider.getPassword()));
        }
        Provider updatedProvider = providerRepository.save(provider);
        return Optional.of(updatedProvider);
    }

    public boolean deleteProvider(Long providerId) {
        ownerCheck.verifyOwnerOrAdmin(providerId);

        return providerRepository.findById(providerId).map(provider -> {
            providerRepository.delete(provider);
            return true;
        }).orElse(false);
    }
}
