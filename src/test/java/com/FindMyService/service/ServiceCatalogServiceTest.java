package com.FindMyService.service;

import com.FindMyService.model.Provider;
import com.FindMyService.model.ServiceCatalog;
import com.FindMyService.model.dto.ServiceCatalogDto;
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

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private FeedbackRepository feedbackRepository;

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
        List<ServiceCatalog> services = Arrays.asList(testService, new ServiceCatalog());
        when(serviceCatalogRepository.findAllWithProvider()).thenReturn(services);

        List<ServiceCatalogDto> result = serviceCatalogService.getAllServices();

        assertThat(result).hasSize(2);
        verify(serviceCatalogRepository).findAllWithProvider();
    }

    @Test
    void getServiceByIdWithValidIdReturnsService() {
        when(serviceCatalogRepository.findByIdWithProvider(1L)).thenReturn(Optional.of(testService));

        Optional<ServiceCatalogDto> result = serviceCatalogService.getServiceById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getServiceId()).isEqualTo(1L);
        verify(serviceCatalogRepository).findByIdWithProvider(1L);
    }

    @Test
    void getServiceByIdWithInvalidIdReturnsEmpty() {
        when(serviceCatalogRepository.findByIdWithProvider(999L)).thenReturn(Optional.empty());

        Optional<ServiceCatalogDto> result = serviceCatalogService.getServiceById(999L);

        assertThat(result).isEmpty();
        verify(serviceCatalogRepository).findByIdWithProvider(999L);
    }

    @Test
    void getServicesByProviderWithValidProviderIdReturnsServices() {
        when(providerRepository.existsById(1L)).thenReturn(true);
        when(serviceCatalogRepository.findByProviderIdWithProvider(1L)).thenReturn(Arrays.asList(testService));

        List<ServiceCatalogDto> result = serviceCatalogService.getServicesByProvider(1L);

        assertThat(result).hasSize(1);
        verify(serviceCatalogRepository).findByProviderIdWithProvider(1L);
    }

    @Test
    void getServicesByProviderWithNullProviderIdReturnsEmptyList() {
        List<ServiceCatalogDto> result = serviceCatalogService.getServicesByProvider(null);

        assertThat(result).isEmpty();
        verify(serviceCatalogRepository, never()).findByProviderIdWithProvider(any());
    }

    @Test
    void getServicesByProviderWithInvalidProviderIdReturnsEmptyList() {
        when(providerRepository.existsById(999L)).thenReturn(false);

        List<ServiceCatalogDto> result = serviceCatalogService.getServicesByProvider(999L);

        assertThat(result).isEmpty();
        verify(serviceCatalogRepository, never()).findByProviderIdWithProvider(any());
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

    @Test
    void deleteServiceWithInvalidIdThrowsException() {
        when(serviceCatalogRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> serviceCatalogService.deleteService(999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Service not found with id: 999");

        verify(serviceCatalogRepository, never()).delete(any());
    }

    @Test
    void updateServiceWithInvalidProviderIdThrowsException() {
        when(serviceCatalogRepository.findById(1L)).thenReturn(Optional.of(testService));
        when(providerRepository.findById(999L)).thenReturn(Optional.empty());
        testDto.setProviderId(999L);

        assertThatThrownBy(() -> serviceCatalogService.updateService(1L, testDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Provider from payload not found");

        verify(serviceCatalogRepository, never()).save(any());
    }
}
