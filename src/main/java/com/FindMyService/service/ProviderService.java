package com.FindMyService.service;

import com.FindMyService.model.Provider;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class ProviderService {

    public List<Provider> getAllProviders() {
        return Collections.emptyList();
    }

    public Optional<Provider> getProviderById(String providerId) {
        return Optional.empty();
    }

    public Provider createProvider(Provider provider) {
        return provider;
    }

    public Optional<Provider> updateProvider(String providerId, Provider provider) {
        return Optional.empty();
    }

    public boolean deleteProvider(String providerId) {
        return false;
    }
}
