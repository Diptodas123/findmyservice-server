package com.FindMyService.service;

import com.FindMyService.model.Provider;
import com.FindMyService.model.dto.ProviderDto;
import com.FindMyService.repository.FeedbackRepository;
import com.FindMyService.repository.OrderRepository;
import com.FindMyService.repository.ProviderRepository;
import com.FindMyService.repository.ServiceCatalogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProviderServiceTest {

    @Mock private ProviderRepository providerRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private FeedbackRepository feedbackRepository;
    @Mock private OrderRepository orderRepository;
    @Mock private ServiceCatalogRepository serviceCatalogRepository;

    @InjectMocks
    private ProviderService providerService;

    private Provider testProvider;

    @BeforeEach
    void setUp() {
        testProvider = Provider.builder()
                .providerId(1L)
                .email("provider@example.com")
                .password("password123")
                .providerName("Test Provider")
                .build();
    }

    @Test
    void getAllProvidersReturnsAll() {
        when(providerRepository.findAll()).thenReturn(List.of(testProvider, new Provider()));
        assertThat(providerService.getAllProviders()).hasSize(2);
    }

    @Test
    void getProviderByIdFound() {
        when(providerRepository.findById(1L)).thenReturn(Optional.of(testProvider));
        assertThat(providerService.getProviderById(1L)).isPresent();
    }

    @Test
    void getProviderByIdNotFound() {
        when(providerRepository.findById(999L)).thenReturn(Optional.empty());
        assertThat(providerService.getProviderById(999L)).isEmpty();
    }

    @Test
    void createProviderReturnsCreated() {
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(providerRepository.save(any())).thenReturn(testProvider);

        ResponseEntity<?> response = providerService.createProvider(testProvider);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        verify(providerRepository).save(any());
    }

    @Test
    void createProviderNullEmailReturnsBadRequest() {
        testProvider.setEmail(null);
        assertThat(providerService.createProvider(testProvider).getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verify(providerRepository, never()).save(any());
    }

    @Test
    void createProviderEmptyEmailReturnsBadRequest() {
        testProvider.setEmail("");
        assertThat(providerService.createProvider(testProvider).getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verify(providerRepository, never()).save(any());
    }

    @Test
    void createProviderNullPasswordReturnsBadRequest() {
        testProvider.setPassword(null);
        assertThat(providerService.createProvider(testProvider).getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verify(providerRepository, never()).save(any());
    }

    @Test
    void createProviderEmptyPasswordReturnsBadRequest() {
        testProvider.setPassword("");
        assertThat(providerService.createProvider(testProvider).getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verify(providerRepository, never()).save(any());
    }

    @Test
    void createProviderRepositoryExceptionReturnsServerError() {
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(providerRepository.save(any())).thenThrow(new RuntimeException("DB error"));
        assertThat(providerService.createProvider(testProvider).getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void updateProviderPatchesFields() {
        when(providerRepository.findById(1L)).thenReturn(Optional.of(testProvider));
        when(providerRepository.save(any())).thenReturn(testProvider);

        ProviderDto patch = new ProviderDto();
        patch.setPhone("555-9999");

        ProviderDto result = providerService.updateProvider(1L, patch);

        assertThat(result).isNotNull();
        verify(providerRepository).save(testProvider);
    }

    @Test
    void updateProviderNotFoundThrows() {
        when(providerRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> providerService.updateProvider(99L, new ProviderDto()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Provider not found");
    }

    @Test
    void deleteProviderSuccess() {
        when(providerRepository.findById(1L)).thenReturn(Optional.of(testProvider));
        providerService.deleteProvider(1L);
        verify(providerRepository).delete(testProvider);
    }

    @Test
    void deleteProviderNotFoundThrows() {
        when(providerRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> providerService.deleteProvider(99L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Provider not found");
    }

    @Test
    void updateProviderPasswordChangeSuccess() {
        testProvider.setPassword("encodedOld");
        when(providerRepository.findById(1L)).thenReturn(Optional.of(testProvider));
        when(passwordEncoder.matches("oldpass", "encodedOld")).thenReturn(true);
        when(passwordEncoder.encode("newpass")).thenReturn("encodedNew");
        when(providerRepository.save(any())).thenReturn(testProvider);

        ProviderDto patch = new ProviderDto();
        patch.setCurrentPassword("oldpass");
        patch.setPassword("newpass");

        providerService.updateProvider(1L, patch);

        verify(passwordEncoder).encode("newpass");
    }

    @Test
    void updateProviderWrongCurrentPasswordThrows() {
        testProvider.setPassword("encodedOld");
        when(providerRepository.findById(1L)).thenReturn(Optional.of(testProvider));
        when(passwordEncoder.matches("wrongpass", "encodedOld")).thenReturn(false);

        ProviderDto patch = new ProviderDto();
        patch.setCurrentPassword("wrongpass");
        patch.setPassword("newpass");

        assertThatThrownBy(() -> providerService.updateProvider(1L, patch))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Current password is incorrect");
    }

    @Test
    void updateProviderMissingCurrentPasswordThrows() {
        when(providerRepository.findById(1L)).thenReturn(Optional.of(testProvider));

        ProviderDto patch = new ProviderDto();
        patch.setPassword("newpass");

        assertThatThrownBy(() -> providerService.updateProvider(1L, patch))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Current password is required");
    }
}