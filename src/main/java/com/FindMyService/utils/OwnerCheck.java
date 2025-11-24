package com.FindMyService.utils;

import com.FindMyService.repository.ProviderRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.function.Function;

@Component
public class OwnerCheck {

    private final ProviderRepository providerRepository;

    public OwnerCheck(ProviderRepository providerRepository) {
        this.providerRepository = providerRepository;
    }

    public void verifyOwnerOrAdmin(Long providerId) {
        verifyOwnerOrAdmin(providerId, providerRepository::findById);
    }

    public boolean isOwnerOrAdmin(Long id, Function<Long, Optional<?>> finder) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return false;

        String principal = auth.getName();
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equalsIgnoreCase(a.getAuthority()) || "ADMIN".equalsIgnoreCase(a.getAuthority()));
        if (isAdmin) return true;

        if (id == null || principal == null) return false;

        return finder.apply(id)
                .map(entity -> {
                    String owner = extractOwnerIdentifier(entity);
                    return owner != null && owner.equalsIgnoreCase(principal);
                })
                .orElse(false);
    }

    public void verifyOwnerOrAdmin(Long id, Function<Long, Optional<?>> finder) {
        if (!isOwnerOrAdmin(id, finder)) {
            throw new AccessDeniedException("Forbidden: not owner or admin");
        }
    }

    private String extractOwnerIdentifier(Object entity) {
        String[] candidates = {"getOwnerEmail", "getOwner", "getOwnerUsername", "getEmail", "getUsername"};
        for (String name : candidates) {
            try {
                Method m = entity.getClass().getMethod(name);
                Object val = m.invoke(entity);
                if (val != null) return val.toString();
            } catch (ReflectiveOperationException ignored) {
            }
        }
        return null;
    }
}
