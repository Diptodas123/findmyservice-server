package com.FindMyService.service;

import com.FindMyService.model.Provider;
import com.FindMyService.model.User;
import com.FindMyService.model.dto.LoginRequestDto;
import com.FindMyService.model.dto.RegisterRequestDto;
import com.FindMyService.model.enums.Role;
import com.FindMyService.repository.ProviderRepository;
import com.FindMyService.repository.UserRepository;
import com.FindMyService.security.JwtTokenUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProviderRepository providerRepository;

    @Mock
    private UserService userService;

    @Mock
    private ProviderService providerService;

    @Mock
    private PasswordEncoder encoder;

    @Mock
    private JwtTokenUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    private RegisterRequestDto registerRequest;
    private LoginRequestDto loginRequest;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequestDto();
        registerRequest.setName("Test User");
        registerRequest.setEmail("test@example.com");
        registerRequest.setPassword("password123");
        registerRequest.setRole(Role.USER);

        loginRequest = new LoginRequestDto();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password123");
    }

    @Test
    void registerWithNullRoleReturnsBadRequest() {
        // Given
        registerRequest.setRole(null);

        // When
        ResponseEntity<Map<String, Object>> response = authService.register(registerRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        Map<String, Object> body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.get("message")).isEqualTo("Role must be provided");
    }

    @Test
    void registerWithExistingUserEmailReturnsConflict() {
        // Given
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        // When
        ResponseEntity<Map<String, Object>> response = authService.register(registerRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        verify(userRepository).existsByEmail("test@example.com");
    }

    @Test
    void registerWithNewUserEmailReturnsCreated() {
        // Given
        registerRequest.setRole(Role.USER);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        
        User savedUser = User.builder()
                .userId(1L)
                .email("test@example.com")
                .name("Test User")
                .password("encodedPassword")
                .role(Role.USER)
                .build();
        
        when(userService.createUser(any(User.class)))
                .thenAnswer(invocation -> ResponseEntity.status(HttpStatus.CREATED).body(savedUser));

        // When
        ResponseEntity<Map<String, Object>> response = authService.register(registerRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("message")).isEqualTo("User registered successfully");
        verify(userService).createUser(any(User.class));
    }

    @Test
    void registerWithExistingProviderEmailReturnsConflict() {
        // Given
        registerRequest.setRole(Role.PROVIDER);
        when(providerRepository.existsByEmail(anyString())).thenReturn(true);

        // When
        ResponseEntity<Map<String, Object>> response = authService.register(registerRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        verify(providerRepository).existsByEmail("test@example.com");
    }

    @Test
    void registerWithNewProviderEmailReturnsCreated() {
        // Given
        registerRequest.setRole(Role.PROVIDER);
        when(providerRepository.existsByEmail(anyString())).thenReturn(false);
        
        Provider savedProvider = Provider.builder()
                .providerId(1L)
                .email("test@example.com")
                .providerName("Test User")
                .password("encodedPassword")
                .build();
        
        when(providerService.createProvider(any(Provider.class)))
                .thenAnswer(invocation -> ResponseEntity.status(HttpStatus.CREATED).body(savedProvider));

        // When
        ResponseEntity<Map<String, Object>> response = authService.register(registerRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("message")).isEqualTo("Provider registered successfully");
        verify(providerService).createProvider(any(Provider.class));
    }

    @Test
    void loginWithInvalidCredentialsReturnsUnauthorized() {
        // Given
        loginRequest.setRole(Role.USER);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // When
        ResponseEntity<Map<String, Object>> response = authService.login(loginRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        Map<String, Object> body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.get("message")).isEqualTo("Invalid email or password");
    }

    @Test
    void loginWithValidUserCredentialsReturnsToken() {
        // Given
        User user = new User();
        user.setUserId(1L);
        user.setEmail("test@example.com");
        user.setPassword("encodedPassword");
        user.setRole(Role.USER);

        loginRequest.setRole(Role.USER);

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(encoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwtUtil.generateToken(anyString(), anyString(), any(Role.class))).thenReturn("jwt-token");

        // When
        ResponseEntity<Map<String, Object>> response = authService.login(loginRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Map<String, Object> body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.get("token")).isEqualTo("jwt-token");
        assertThat(body.get("userId")).isEqualTo(1L);
        verify(jwtUtil).generateToken(eq("1"), eq(user.getEmail()), eq(Role.USER));
    }

    @Test
    void loginWithValidProviderCredentialsReturnsToken() {
        // Given
        Provider provider = new Provider();
        provider.setProviderId(1L);
        provider.setEmail("provider@example.com");
        provider.setPassword("encodedPassword");

        loginRequest.setEmail("provider@example.com");
        loginRequest.setRole(Role.PROVIDER);

        when(providerRepository.findByEmail(anyString())).thenReturn(Optional.of(provider));
        when(encoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwtUtil.generateToken(anyString(), anyString(), any(Role.class))).thenReturn("jwt-token");

        // When
        ResponseEntity<Map<String, Object>> response = authService.login(loginRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Map<String, Object> body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.get("token")).isEqualTo("jwt-token");
        assertThat(body.get("providerId")).isEqualTo(1L);
        verify(jwtUtil).generateToken(eq("1"), eq(provider.getEmail()), eq(Role.PROVIDER));
    }

    @Test
    void loginWithWrongPasswordReturnsUnauthorized() {
        // Given
        User user = new User();
        user.setUserId(1L);
        user.setEmail("test@example.com");
        user.setPassword("encodedPassword");

        loginRequest.setRole(Role.USER);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(encoder.matches(anyString(), anyString())).thenReturn(false);

        // When
        ResponseEntity<Map<String, Object>> response = authService.login(loginRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        verify(jwtUtil, never()).generateToken(anyString(), anyString(), any(Role.class));
    }
}
