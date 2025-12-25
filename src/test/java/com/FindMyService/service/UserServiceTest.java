package com.FindMyService.service;

import com.FindMyService.model.User;
import com.FindMyService.repository.UserRepository;
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
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private OwnerCheck ownerCheck;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUserId(1L);
        testUser.setEmail("test@example.com");
        testUser.setPassword("password123");
        testUser.setName("Test User");
        
        ReflectionTestUtils.setField(userService, "passwordEncoder", passwordEncoder);
    }

    @Test
    void getAllUsersReturnsListOfUsers() {
        // Given
        List<User> users = Arrays.asList(testUser, new User());
        when(userRepository.findAll()).thenReturn(users);

        // When
        List<User> result = userService.getAllUsers();

        // Then
        assertThat(result).hasSize(2);
        verify(userRepository).findAll();
    }

    @Test
    void getUserByIdWithValidIdReturnsUser() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // When
        Optional<User> result = userService.getUserById(1L);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getUserId()).isEqualTo(1L);
        verify(userRepository).findById(1L);
    }

    @Test
    void getUserByIdWithInvalidIdReturnsEmpty() {
        // Given
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // When
        Optional<User> result = userService.getUserById(999L);

        // Then
        assertThat(result).isEmpty();
        verify(userRepository).findById(999L);
    }

    @Test
    void createUserWithValidDataReturnsCreated() {
        // Given
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        ResponseEntity<?> response = userService.createUser(testUser);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUserWithNullEmailReturnsBadRequest() {
        // Given
        testUser.setEmail(null);

        // When
        ResponseEntity<?> response = userService.createUser(testUser);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void createUserWithEmptyEmailReturnsBadRequest() {
        // Given
        testUser.setEmail("");

        // When
        ResponseEntity<?> response = userService.createUser(testUser);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void createUserWithNullPasswordReturnsBadRequest() {
        // Given
        testUser.setPassword(null);

        // When
        ResponseEntity<?> response = userService.createUser(testUser);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void createUserWithEmptyPasswordReturnsBadRequest() {
        // Given
        testUser.setPassword("");

        // When
        ResponseEntity<?> response = userService.createUser(testUser);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void createUserWithRepositoryExceptionReturnsInternalServerError() {
        // Given
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenThrow(new RuntimeException("Database error"));

        // When
        ResponseEntity<?> response = userService.createUser(testUser);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
