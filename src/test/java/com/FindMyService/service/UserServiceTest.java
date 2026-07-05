package com.FindMyService.service;

import com.FindMyService.model.User;
import com.FindMyService.model.dto.UserDto;
import com.FindMyService.model.enums.Role;
import com.FindMyService.repository.FeedbackRepository;
import com.FindMyService.repository.OrderRepository;
import com.FindMyService.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private OrderRepository orderRepository;
    @Mock private FeedbackRepository feedbackRepository;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .userId(1L)
                .email("test@example.com")
                .password("password123")
                .name("Test User")
                .role(Role.USER)
                .build();
    }

    @Test
    void getAllUsersReturnsAll() {
        when(userRepository.findAll()).thenReturn(List.of(testUser, new User()));
        assertThat(userService.getAllUsers()).hasSize(2);
    }

    @Test
    void getUserByIdFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        assertThat(userService.getUserById(1L)).isPresent();
    }

    @Test
    void getUserByIdNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());
        assertThat(userService.getUserById(999L)).isEmpty();
    }

    @Test
    void createUserReturnsCreated() {
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(userRepository.save(any())).thenReturn(testUser);

        ResponseEntity<?> response = userService.createUser(testUser);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        verify(userRepository).save(any());
    }

    @Test
    void createUserNullEmailReturnsBadRequest() {
        testUser.setEmail(null);
        assertThat(userService.createUser(testUser).getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verify(userRepository, never()).save(any());
    }

    @Test
    void createUserEmptyEmailReturnsBadRequest() {
        testUser.setEmail("");
        assertThat(userService.createUser(testUser).getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verify(userRepository, never()).save(any());
    }

    @Test
    void createUserNullPasswordReturnsBadRequest() {
        testUser.setPassword(null);
        assertThat(userService.createUser(testUser).getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verify(userRepository, never()).save(any());
    }

    @Test
    void createUserEmptyPasswordReturnsBadRequest() {
        testUser.setPassword("");
        assertThat(userService.createUser(testUser).getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verify(userRepository, never()).save(any());
    }

    @Test
    void createUserRepositoryExceptionReturnsServerError() {
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(userRepository.save(any())).thenThrow(new RuntimeException("DB error"));
        assertThat(userService.createUser(testUser).getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void deleteUserRemovesDependenciesAndUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        userService.deleteUser(1L);

        verify(feedbackRepository).deleteByUser(testUser);
        verify(orderRepository).deleteByUser(testUser);
        verify(userRepository).delete(testUser);
    }

    @Test
    void deleteUserNotFoundThrows() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> userService.deleteUser(99L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void updateUserPatchesFields() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any())).thenReturn(testUser);

        UserDto patch = new UserDto();
        patch.setName("New Name");
        patch.setPhone("555-1234");

        UserDto result = userService.updateUser(1L, patch);

        assertThat(result).isNotNull();
        verify(userRepository).save(testUser);
    }

    @Test
    void updateUserNotFoundThrows() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> userService.updateUser(99L, new UserDto()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void updateUserPasswordChangeSuccess() {
        testUser.setPassword("encodedOld");
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("oldpass", "encodedOld")).thenReturn(true);
        when(passwordEncoder.encode("newpass")).thenReturn("encodedNew");
        when(userRepository.save(any())).thenReturn(testUser);

        UserDto patch = new UserDto();
        patch.setCurrentPassword("oldpass");
        patch.setPassword("newpass");

        userService.updateUser(1L, patch);

        verify(passwordEncoder).encode("newpass");
    }

    @Test
    void updateUserWrongCurrentPasswordThrows() {
        testUser.setPassword("encodedOld");
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongpass", "encodedOld")).thenReturn(false);

        UserDto patch = new UserDto();
        patch.setCurrentPassword("wrongpass");
        patch.setPassword("newpass");

        assertThatThrownBy(() -> userService.updateUser(1L, patch))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Current password is incorrect");
    }

    @Test
    void updateUserMissingCurrentPasswordThrows() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        UserDto patch = new UserDto();
        patch.setPassword("newpass"); // no currentPassword

        assertThatThrownBy(() -> userService.updateUser(1L, patch))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Current password is required");
    }

    @Test
    void updateUserRoleChangeByNonAdminThrows() {
        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("u@test.com", null, "USER"));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        UserDto patch = new UserDto();
        patch.setRole(Role.ADMIN);

        assertThatThrownBy(() -> userService.updateUser(1L, patch))
                .isInstanceOf(ResponseStatusException.class);

        SecurityContextHolder.clearContext();
    }

    @Test
    void updateUserRoleChangeByAdminSucceeds() {
        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("admin@test.com", null, "ADMIN"));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any())).thenReturn(testUser);

        UserDto patch = new UserDto();
        patch.setRole(Role.ADMIN);

        userService.updateUser(1L, patch);

        assertThat(testUser.getRole()).isEqualTo(Role.ADMIN);
        SecurityContextHolder.clearContext();
    }
}