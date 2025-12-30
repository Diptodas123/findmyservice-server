package com.FindMyService.service;

import com.FindMyService.model.Provider;
import com.FindMyService.model.ServiceCatalog;
import com.FindMyService.model.dto.ServiceCatalogDto;
import com.FindMyService.repository.ProviderRepository;
import com.FindMyService.repository.ServiceCatalogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServiceCatalogServiceTest {

    @Mock
    private ServiceCatalogRepository serviceCatalogRepository;

    @Mock
    private ProviderRepository providerRepository;

    @InjectMocks
    private ServiceCatalogService serviceCatalogService;

    private ServiceCatalog testService;
    private Provider testProvider;
    private ServiceCatalogDto testDto;

    @BeforeEach
    void setUp() {
        testProvider = new Provider();
        testProvider.setProviderId(1L);
        testProvider.setProviderName("Test Provider");

        testService = new ServiceCatalog();
        testService.setServiceId(1L);
        testService.setServiceName("Test Service");
        testService.setProviderId(testProvider);

        testDto = new ServiceCatalogDto();
        testDto.setServiceName("Test Service");
        testDto.setProviderId(1L);
    }

    @Test
    void getAllServicesReturnsListOfServices() {
        // Given
        List<ServiceCatalog> services = Arrays.asList(testService, new ServiceCatalog());
        when(serviceCatalogRepository.findAll()).thenReturn(services);

        // When
        List<ServiceCatalog> result = serviceCatalogService.getAllServices();

        // Then
        assertThat(result).hasSize(2);
        verify(serviceCatalogRepository).findAll();
    }

    @Test
    void getServiceByIdWithValidIdReturnsService() {
        // Given
        when(serviceCatalogRepository.findById(1L)).thenReturn(Optional.of(testService));

        // When
        Optional<ServiceCatalog> result = serviceCatalogService.getServiceById(1L);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getServiceId()).isEqualTo(1L);
        verify(serviceCatalogRepository).findById(1L);
    }

    @Test
    void getServiceByIdWithInvalidIdReturnsEmpty() {
        // Given
        when(serviceCatalogRepository.findById(999L)).thenReturn(Optional.empty());

        // When
        Optional<ServiceCatalog> result = serviceCatalogService.getServiceById(999L);

        // Then
        assertThat(result).isEmpty();
        verify(serviceCatalogRepository).findById(999L);
    }

    @Test
    void getServicesByProviderWithValidProviderIdReturnsServices() {
        // Given
        when(providerRepository.existsById(1L)).thenReturn(true);
        when(serviceCatalogRepository.findByProviderId_ProviderId(1L))
                .thenReturn(Arrays.asList(testService));

        // When
        List<ServiceCatalog> result = serviceCatalogService.getServicesByProvider(1L);

        // Then
        assertThat(result).hasSize(1);
        verify(serviceCatalogRepository).findByProviderId_ProviderId(1L);
    }

    @Test
    void getServicesByProviderWithNullProviderIdReturnsEmptyList() {
        // When
        List<ServiceCatalog> result = serviceCatalogService.getServicesByProvider(null);

        // Then
        assertThat(result).isEmpty();
        verify(serviceCatalogRepository, never()).findByProviderId_ProviderId(any());
    }

    @Test
    void getServicesByProviderWithInvalidProviderIdReturnsEmptyList() {
        // Given
        when(providerRepository.existsById(999L)).thenReturn(false);

        // When
        List<ServiceCatalog> result = serviceCatalogService.getServicesByProvider(999L);

        // Then
        assertThat(result).isEmpty();
        verify(serviceCatalogRepository, never()).findByProviderId_ProviderId(any());
    }

    @Test
    void createServiceWithValidDataReturnsCreatedService() {
        // Given
        when(providerRepository.findById(1L)).thenReturn(Optional.of(testProvider));
        when(serviceCatalogRepository.save(any(ServiceCatalog.class))).thenReturn(testService);

        // When
        ServiceCatalogDto result = serviceCatalogService.createService(testDto);

        // Then
        assertThat(result).isNotNull();
        verify(providerRepository).findById(1L);
        verify(serviceCatalogRepository).save(any(ServiceCatalog.class));
    }

    @Test
    void createServiceWithInvalidProviderIdThrowsException() {
        // Given
        when(providerRepository.findById(999L)).thenReturn(Optional.empty());
        testDto.setProviderId(999L);

        // When/Then
        assertThatThrownBy(() -> serviceCatalogService.createService(testDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Provider from payload not found");
        
        verify(serviceCatalogRepository, never()).save(any());
    }

    @Test
    void updateServiceWithValidDataReturnsUpdatedService() {
        // Given
        when(serviceCatalogRepository.findById(1L)).thenReturn(Optional.of(testService));
        when(providerRepository.findById(1L)).thenReturn(Optional.of(testProvider));
        when(serviceCatalogRepository.save(any(ServiceCatalog.class))).thenReturn(testService);

        // When
        ServiceCatalogDto result = serviceCatalogService.updateService(1L, testDto);

        // Then
        assertThat(result).isNotNull();
        verify(serviceCatalogRepository).save(any(ServiceCatalog.class));
    }

    @Test
    void updateServiceWithInvalidServiceIdThrowsException() {
        // Given
        when(serviceCatalogRepository.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> serviceCatalogService.updateService(999L, testDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Service not found with id: 999");
        
        verify(serviceCatalogRepository, never()).save(any());
    }

    @Test
    void deleteServiceWithValidIdDeletesService() {
        // Given
        when(serviceCatalogRepository.findById(1L)).thenReturn(Optional.of(testService));
        doNothing().when(serviceCatalogRepository).delete(testService);

        // When
        serviceCatalogService.deleteService(1L);

        // Then
        verify(serviceCatalogRepository).findById(1L);
        verify(serviceCatalogRepository).delete(any(ServiceCatalog.class));
    }
}
