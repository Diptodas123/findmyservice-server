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

                Provider provider = new Provider();
                provider.setProviderName(request.getName());
                provider.setEmail(request.getEmail());
                provider.setPassword(request.getPassword());
                provider.setPhone(request.getPhone());
                provider.setAddressLine1(request.getAddressLine1());
                provider.setAddressLine2(request.getAddressLine2());
                provider.setCity(request.getCity());
                provider.setState(request.getState());
                provider.setZipCode(request.getZipCode());

                Provider created = providerService.createProvider(provider);
                if (created == null || Objects.equals(created.getEmail(), null)) {
                    return Map.of("error", "Unable to register provider");
                }
            }
            case USER, ADMIN -> {
                if (userRepository.existsByEmail(request.getEmail())) {
                    return Map.of("error", "Email already registered");
                }
                User user = new User();
                user.setName(request.getName());
                user.setEmail(request.getEmail());
                user.setPassword(request.getPassword());
                user.setRole(request.getRole());
                user.setPhone(request.getPhone());
                user.setAddressLine1(request.getAddressLine1());
                user.setAddressLine2(request.getAddressLine2());
                user.setCity(request.getCity());
                user.setState(request.getState());
                user.setZipCode(request.getZipCode());

                User created = userService.createUser(user);
                if (created == null || Objects.equals(created.getUserId(), null)) {
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
                var provider = providerRepository.findByEmail(request.getEmail()).orElse(null);

                if (provider == null || !encoder.matches(request.getPassword(), provider.getPassword())) {
                    return Map.of("error", "Invalid email or password");
                }
                String token = jwtUtil.generateToken(provider.getProviderId().toString(), provider.getEmail(), Role.PROVIDER);
                return Map.of("token", token);
            }
            case USER, ADMIN -> {
                var user = userRepository.findByEmail(request.getEmail()).orElse(null);

                if (user == null || !encoder.matches(request.getPassword(), user.getPassword())) {
                    return Map.of("error", "Invalid email or password");
                }
                if(request.getRole() != user.getRole()) {
                    return Map.of("error", "Invalid role for user");
                }
                String token = jwtUtil.generateToken(user.getUserId().toString(), user.getEmail(), user.getRole());
                return Map.of("token", token);
            }
            default -> {
                return Map.of("error", "Invalid role");
            }
        }


    }
}
