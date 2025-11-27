package com.FindMyService.service;

import com.FindMyService.model.User;
import com.FindMyService.model.Provider;
import com.FindMyService.model.dto.LoginRequestDto;
import com.FindMyService.model.dto.RegisterRequestDto;
import com.FindMyService.model.enums.Role;
import com.FindMyService.repository.ProviderRepository;
import com.FindMyService.repository.UserRepository;
import com.FindMyService.security.JwtTokenUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;

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

    public Map<String, String> register(RegisterRequestDto request) {
        Role role = request.getRole();
        if (role == null) {
            return Map.of("error", "Role must be provided");
        }

        switch (role) {
            case PROVIDER -> {
                if (providerRepository.existsByEmail(request.getEmail())) {
                    return Map.of("error", "Email already registered");
                }

                Provider provider = Provider
                        .builder()
                        .providerName(request.getName())
                        .email(request.getEmail())
                        .password(request.getPassword())
                        .build();

                Provider newProvider = providerService.createProvider(provider);
                if (newProvider == null || Objects.equals(newProvider.getEmail(), null)) {
                    return Map.of("error", "Unable to register provider");
                }
            }
            case USER -> {
                if (userRepository.existsByEmail(request.getEmail())) {
                    return Map.of("error", "Email already registered");
                }
                User user = User
                        .builder()
                        .name(request.getName())
                        .email(request.getEmail())
                        .password(request.getPassword())
                        .build();

                User newUser = userService.createUser(user);
                if (newUser == null || Objects.equals(newUser.getUserId(), null)) {
                    return Map.of("error", "Unable to register user");
                }
            }
            default -> {
                return Map.of("error", "Invalid role");
            }
        }

        return Map.of("message", "User registered successfully");
    }

    public Map<String, String> login(LoginRequestDto request) {
        Role role = request.getRole();

        switch (role) {
            case PROVIDER -> {
                Provider provider = providerRepository.findByEmail(request.getEmail()).orElse(null);

                if (provider == null || !encoder.matches(request.getPassword(), provider.getPassword())) {
                    return Map.of("error", "Invalid email or password");
                }
                String token = jwtUtil.generateToken(provider.getProviderId().toString(), provider.getEmail(), request.getRole());
                return Map.of("token", token);
            }
            case USER, ADMIN -> {
                User user = userRepository.findByEmail(request.getEmail()).orElse(null);

                if (user == null || !encoder.matches(request.getPassword(), user.getPassword())) {
                    return Map.of("error", "Invalid email or password");
                }
                String token = jwtUtil.generateToken(user.getUserId().toString(), user.getEmail(), request.getRole());
                return Map.of("token", token);
            }
            default -> {
                return Map.of("error", "Invalid role");
            }
        }


    }
}
