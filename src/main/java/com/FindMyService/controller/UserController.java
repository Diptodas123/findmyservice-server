package com.FindMyService.controller;

import java.util.List;
import java.util.stream.Collectors;
import com.FindMyService.model.dto.UserDto;
import com.FindMyService.utils.DtoMapper;
import com.FindMyService.utils.ResponseBuilder;
import com.FindMyService.utils.OwnerCheck;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import com.FindMyService.model.User;
import com.FindMyService.service.UserService;

@RequestMapping("/api/v1/users")
@RestController
public class UserController {

    private final UserService userService;
    private final OwnerCheck ownerCheck;

    public UserController(UserService userService, OwnerCheck ownerCheck) {
        this.userService = userService;
        this.ownerCheck = ownerCheck;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        List<UserDto> dtos = userService.getAllUsers()
                .stream()
                .map(DtoMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<?> getUser(@PathVariable Long userId) {
        return userService.getUserById(userId)
                .map(DtoMapper::toDto)
                .map(dto -> ResponseEntity.ok((Object) dto))
                .orElseGet(() -> ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(ResponseBuilder.build(HttpStatus.NOT_FOUND, "User not found")));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> createUser(@RequestBody User user) {
        return userService.createUser(user);
    }

    @PatchMapping("/{userId}")
    @PreAuthorize("hasAuthority('USER') or hasAuthority('ADMIN')")
    public ResponseEntity<?> updateUser(@PathVariable Long userId, @RequestBody UserDto userDto) {
        try {
            ownerCheck.verifyOwner(userId);
            UserDto updatedUser = userService.updateUser(userId, userDto);
            return ResponseEntity.ok(updatedUser);
        } catch (AccessDeniedException ex) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(ResponseBuilder.forbidden("You are not authorized to update this user"));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ResponseBuilder.notFound(ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResponseBuilder.internalServerError("Failed to update user: " + ex.getMessage()));
        }
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("hasAuthority('USER') or hasAuthority('ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable Long userId) {
        try {
            ownerCheck.verifyOwner(userId);
            userService.deleteUser(userId);
            return ResponseEntity.ok(ResponseBuilder.ok("User deleted successfully"));
        } catch (AccessDeniedException ex) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(ResponseBuilder.forbidden("You are not authorized to delete this user"));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ResponseBuilder.notFound(ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResponseBuilder.internalServerError("Failed to delete user: " + ex.getMessage()));
        }
    }
}
