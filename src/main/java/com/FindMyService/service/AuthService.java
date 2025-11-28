package com.FindMyService.service;

import com.FindMyService.model.User;
import com.FindMyService.model.Provider;
import com.FindMyService.model.dto.LoginRequestDto;
import com.FindMyService.model.dto.RegisterRequestDto;
import com.FindMyService.model.enums.Role;
import com.FindMyService.repository.ProviderRepository;
import com.FindMyService.repository.UserRepository;
import com.FindMyService.security.JwtTokenUtil;
import com.FindMyService.utils.ErrorResponseBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Map;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final ProviderRepository providerRepository;
    private final UserService userService;
    private final ProviderService providerService;
    private final PasswordEncoder encoder;
    private final JwtTokenUtil jwtUtil;

    public AuthService(UserRepository userRepository,
                       UserService userService,
                       ProviderRepository providerRepository,
                       ProviderService providerService,
                       PasswordEncoder encoder,
                       JwtTokenUtil jwtUtil) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.providerRepository = providerRepository;
        this.providerService = providerService;
        this.encoder = encoder;
        this.jwtUtil = jwtUtil;
    }

    public ResponseEntity<Map<String, Object>> register(RegisterRequestDto request) {
        Role role = request.getRole();
        if (role == null) {
            return ResponseEntity
                    .badRequest()
                    .body(ErrorResponseBuilder.build(HttpStatus.BAD_REQUEST, "Role must be provided"));
        }

        switch (role) {
            case PROVIDER -> {
                if (providerRepository.existsByEmail(request.getEmail())) {
                    return ResponseEntity
                            .status(HttpStatus.CONFLICT)
                            .body(ErrorResponseBuilder.conflict("Email already registered"));
                }

                Provider provider = Provider
                        .builder()
                        .providerName(request.getName())
                        .email(request.getEmail())
                        .password(request.getPassword())
                        .build();

                ResponseEntity<?> response = providerService.createProvider(provider);
                if (response.getStatusCode() != HttpStatus.CREATED) {
                    return ResponseEntity
                            .status(response.getStatusCode())
                            .body((Map<String, Object>) response.getBody());
                }

                return ResponseEntity
                        .status(HttpStatus.CREATED)
                        .body(ErrorResponseBuilder.created("Provider registered successfully"));
            }
            case USER -> {
                if (userRepository.existsByEmail(request.getEmail())) {
                    return ResponseEntity
                            .status(HttpStatus.CONFLICT)
                            .body(ErrorResponseBuilder.conflict("Email already registered"));
                }
                User user = User
                        .builder()
                        .name(request.getName())
                        .email(request.getEmail())
                        .role(request.getRole())
                        .password(request.getPassword())
                        .build();

                ResponseEntity<?> response = userService.createUser(user);
                if (response.getStatusCode() != HttpStatus.CREATED) {
                    return ResponseEntity
                            .status(response.getStatusCode())
                            .body((Map<String, Object>) response.getBody());
                }

                return ResponseEntity
                        .status(HttpStatus.CREATED)
                        .body(ErrorResponseBuilder.created("User registered successfully"));
            }
            default -> {
                return ResponseEntity
                        .badRequest()
                        .body(ErrorResponseBuilder.build(HttpStatus.BAD_REQUEST, "Invalid role"));
            }
        }
    }

    public ResponseEntity<Map<String, Object>> login(LoginRequestDto request) {
        Role role = request.getRole();

        if (role == null) {
            return ResponseEntity
                    .badRequest()
                    .body(ErrorResponseBuilder.build(HttpStatus.BAD_REQUEST, "Role must be provided"));
        }

        switch (role) {
            case PROVIDER -> {
                Provider provider = providerRepository.findByEmail(request.getEmail()).orElse(null);

                if (provider == null || !encoder.matches(request.getPassword(), provider.getPassword())) {
                    return ResponseEntity
                            .status(HttpStatus.UNAUTHORIZED)
                            .body(ErrorResponseBuilder.unauthorized("Invalid email or password"));
                }
                String token = jwtUtil.generateToken(provider.getProviderId().toString(), provider.getEmail(), request.getRole());
                return ResponseEntity.ok(Map.of("token", token, "providerId", provider.getProviderId()));
            }
            case USER, ADMIN -> {
                User user = userRepository.findByEmail(request.getEmail()).orElse(null);

                if (user == null || !encoder.matches(request.getPassword(), user.getPassword())) {
                    return ResponseEntity
                            .status(HttpStatus.UNAUTHORIZED)
                            .body(ErrorResponseBuilder.unauthorized("Invalid email or password"));
                }
                if (request.getRole() != user.getRole()) {
                    return ResponseEntity
                            .status(HttpStatus.FORBIDDEN)
                            .body(ErrorResponseBuilder.forbidden("Invalid role for user"));
                }
                String token = jwtUtil.generateToken(user.getUserId().toString(), user.getEmail(), user.getRole());
                return ResponseEntity.ok(Map.of("token", token, "userId", user.getUserId()));
            }
            default -> {
                return ResponseEntity
                        .badRequest()
                        .body(ErrorResponseBuilder.build(HttpStatus.BAD_REQUEST, "Invalid role"));
            }
        }
    }
}
