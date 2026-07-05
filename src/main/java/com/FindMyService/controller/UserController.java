package com.FindMyService.controller;

import java.util.List;
import com.FindMyService.model.dto.UserDto;
import com.FindMyService.utils.ResponseBuilder;
import com.FindMyService.utils.OwnerCheck;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import com.FindMyService.model.User;
import com.FindMyService.service.UserService;

@RequestMapping("/api/v1/users")
@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final OwnerCheck ownerCheck;

    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{userId}")
    public ResponseEntity<?> getUser(@PathVariable Long userId) {
        return userService.getUserById(userId)
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
        ownerCheck.verifyOwner(userId);
        return ResponseEntity.ok(userService.updateUser(userId, userDto));
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("hasAuthority('USER') or hasAuthority('ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable Long userId) {
        ownerCheck.verifyOwner(userId);
        userService.deleteUser(userId);
        return ResponseEntity.ok(ResponseBuilder.build(HttpStatus.OK, "User deleted successfully"));
    }
}
