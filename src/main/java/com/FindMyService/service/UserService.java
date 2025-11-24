package com.FindMyService.service;

import com.FindMyService.model.User;
import com.FindMyService.repository.UserRepository;
import com.FindMyService.utils.OwnerCheck;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final UserRepository userRepository;
    private final OwnerCheck ownerCheck;

    public UserService(UserRepository userRepository, OwnerCheck ownerCheck) {
        this.userRepository = userRepository;
        this.ownerCheck = ownerCheck;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(Long userId) {
        return userRepository.findById(userId);
    }

    public User createUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public Optional<User> updateUser(Long userId, User user) {
        ownerCheck.verifyOwnerOrAdmin(userId, this::getUserById);

        User existingUser = userRepository.findById(userId).orElse(null);
        if (existingUser == null) {
            return Optional.empty();
        }
        user.setUserId(userId);
        if (user.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        User updatedUser = userRepository.save(user);
        return Optional.of(updatedUser);
    }

    public boolean deleteUser(Long userId) {
        ownerCheck.verifyOwnerOrAdmin(userId, this::getUserById);

        return userRepository.findById(userId).map(user -> {
            userRepository.delete(user);
            return true;
        }).orElse(false);
    }
}
