package com.FindMyService.service;

import com.FindMyService.model.User;
import com.FindMyService.model.dto.UserDto;
import com.FindMyService.repository.FeedbackRepository;
import com.FindMyService.repository.OrderRepository;
import com.FindMyService.repository.UserRepository;
import com.FindMyService.utils.DtoMapper;
import com.FindMyService.utils.ResponseBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.function.Consumer;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final FeedbackRepository feedbackRepository;

    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream().map(DtoMapper::toDto).toList();
    }

    public Optional<UserDto> getUserById(Long userId) {
        return userRepository.findById(userId).map(DtoMapper::toDto);
    }

    @Transactional
    public ResponseEntity<?> createUser(User user) {
        if (user.getEmail() == null || user.getEmail().isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Email is required"));
        }

        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Password is required"));
        }

        try {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            User created = userRepository.save(user);
            return ResponseEntity.status(HttpStatus.CREATED).body(DtoMapper.toDto(created));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResponseBuilder.build(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to create user: " + e.getMessage()));
        }
    }

    @Transactional
    public UserDto updateUser(Long userId, UserDto userDto) {
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        updateIfNotNull(userDto.getName(), existingUser::setName);
        updateIfNotNull(userDto.getEmail(), existingUser::setEmail);
        updateIfNotNull(userDto.getPhone(), existingUser::setPhone);
        updateIfNotNull(userDto.getAddressLine1(), existingUser::setAddressLine1);
        updateIfNotNull(userDto.getAddressLine2(), existingUser::setAddressLine2);
        updateIfNotNull(userDto.getCity(), existingUser::setCity);
        updateIfNotNull(userDto.getState(), existingUser::setState);
        updateIfNotNull(userDto.getZipCode(), existingUser::setZipCode);
        updateIfNotNull(userDto.getProfilePictureUrl(), existingUser::setProfilePictureUrl);

        if (userDto.getRole() != null) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            boolean isAdmin = auth.getAuthorities().stream()
                    .anyMatch(a -> "ADMIN".equals(a.getAuthority()));
            if (!isAdmin) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only admins can change roles");
            }
            existingUser.setRole(userDto.getRole());
        }

        if (userDto.getPassword() != null && !userDto.getPassword().isEmpty()) {
            if (userDto.getCurrentPassword() == null || userDto.getCurrentPassword().isEmpty()) {
                throw new IllegalArgumentException("Current password is required to update password");
            }

            if (!passwordEncoder.matches(userDto.getCurrentPassword(), existingUser.getPassword())) {
                throw new IllegalArgumentException("Current password is incorrect");
            }

            existingUser.setPassword(passwordEncoder.encode(userDto.getPassword()));
        }

        User updated = userRepository.save(existingUser);
        return DtoMapper.toDto(updated);
    }

    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));
        feedbackRepository.deleteByUser(user);
        orderRepository.deleteByUser(user);
        userRepository.delete(user);
    }

    private <T> void updateIfNotNull(T value, Consumer<T> setter) {
        if (value != null) {
            setter.accept(value);
        }
    }
}
