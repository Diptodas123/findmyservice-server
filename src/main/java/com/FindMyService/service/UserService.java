package com.FindMyService.service;

import com.FindMyService.model.User;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    public List<User> getAllUsers() {
        return Collections.emptyList();
    }

    public Optional<User> getUserById(String userId) {
        return Optional.empty();
    }

    public User createUser(User user) {
        return user;
    }

    public Optional<User> updateUser(String userId, User user) {
        return Optional.empty();
    }

    public boolean deleteUser(String userId) {
        return false;
    }
}
