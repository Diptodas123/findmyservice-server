package com.FindMyService.service;

import com.FindMyService.model.Provider;
import com.FindMyService.repository.ProviderRepository;
import com.FindMyService.utils.OwnerCheck;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProviderServiceTest {

    @Mock
    private ProviderRepository providerRepository;

    @Mock
    private OwnerCheck ownerCheck;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private ProviderService providerService;

    private Provider testProvider;

    @BeforeEach
    void setUp() {
        testProvider = new Provider();
        testProvider.setProviderId(1L);
        testProvider.setEmail("provider@example.com");
        testProvider.setPassword("password123");
        testProvider.setProviderName("Test Provider");
        ReflectionTestUtils.setField(providerService, "passwordEncoder", passwordEncoder);
    }

    @Test
    void getAllProvidersReturnsListOfProviders() {
        // Given
        List<Provider> providers = Arrays.asList(testProvider, new Provider());
        when(providerRepository.findAll()).thenReturn(providers);

        // When
        List<Provider> result = providerService.getAllProviders();

        // Then
        assertThat(result).hasSize(2);
        verify(providerRepository).findAll();
    }

    @Test
    void getProviderByIdWithValidIdReturnsProvider() {
        // Given
        when(providerRepository.findById(1L)).thenReturn(Optional.of(testProvider));

        // When
        Optional<Provider> result = providerService.getProviderById(1L);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getProviderId()).isEqualTo(1L);
        verify(providerRepository).findById(1L);
    }

    @Test
    void getProviderByIdWithInvalidIdReturnsEmpty() {
        // Given
        when(providerRepository.findById(999L)).thenReturn(Optional.empty());

        // When
        Optional<Provider> result = providerService.getProviderById(999L);

        // Then
        assertThat(result).isEmpty();
        verify(providerRepository).findById(999L);
    }

    @Test
    void createProviderWithValidDataReturnsCreated() {
        // Given
        Provider inputProvider = new Provider();
        inputProvider.setProviderId(1L);
        inputProvider.setEmail("provider@example.com");
        inputProvider.setPassword("password123");
        inputProvider.setProviderName("Test Provider");

        Provider savedProvider = new Provider();
        savedProvider.setProviderId(1L);
        savedProvider.setEmail("provider@example.com");
        savedProvider.setPassword("encodedPassword");
        savedProvider.setProviderName("Test Provider");

        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(providerRepository.save(any(Provider.class))).thenReturn(savedProvider);

        // When
        ResponseEntity<?> response = providerService.createProvider(inputProvider);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        verify(passwordEncoder).encode("password123");
        verify(providerRepository).save(any(Provider.class));
    }

    @Test
    void createProviderWithNullEmailReturnsBadRequest() {
        // Given
        testProvider.setEmail(null);

        // When
        ResponseEntity<?> response = providerService.createProvider(testProvider);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verify(providerRepository, never()).save(any(Provider.class));
    }

    @Test
    void createProviderWithEmptyEmailReturnsBadRequest() {
        // Given
        testProvider.setEmail("");

        // When
        ResponseEntity<?> response = providerService.createProvider(testProvider);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verify(providerRepository, never()).save(any(Provider.class));
    }

    @Test
    void createProviderWithNullPasswordReturnsBadRequest() {
        // Given
        testProvider.setPassword(null);

        // When
        ResponseEntity<?> response = providerService.createProvider(testProvider);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verify(providerRepository, never()).save(any(Provider.class));
    }

    @Test
    void createProviderWithEmptyPasswordReturnsBadRequest() {
        // Given
        testProvider.setPassword("");

        // When
        ResponseEntity<?> response = providerService.createProvider(testProvider);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verify(providerRepository, never()).save(any(Provider.class));
    }

    @Test
    void createProviderWithRepositoryExceptionReturnsInternalServerError() {
        // Given
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(providerRepository.save(any(Provider.class))).thenThrow(new RuntimeException("Database error"));

        // When
        ResponseEntity<?> response = providerService.createProvider(testProvider);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
