package com.example.bookstore.user.service;

import com.example.bookstore.user.dto.RegisterRequest;
import com.example.bookstore.user.entity.Role;
import com.example.bookstore.user.entity.User;
import com.example.bookstore.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new RuntimeException("Email already exists");
        }
        User user = User.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .firstName(request.firstName())
                .lastName(request.lastName())
                .role(Role.USER)
                .build();
        return userRepository.save(user);
    }

    public User requireUserByEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new RuntimeException("Unauthenticated");
        }
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public User requireUserById(UUID userId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId is required");
        }
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public void ensureDefaultAdminUser() {
        if (userRepository.count() != 0) {
            return;
        }

        User adminUser = User.builder()
                .email("admin@bookstore.com")
                .password(passwordEncoder.encode("AdminPassword123!"))
                .firstName("System")
                .lastName("Administrator")
                .role(Role.ADMIN)
                .build();

        userRepository.save(adminUser);
    }

    public User findOrCreateOauthUser(String email, String firstName, String lastName) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("email is required");
        }
        return userRepository.findByEmail(email).orElseGet(() -> {
            String randomPassword = "OAUTH-" + UUID.randomUUID();
            User user = User.builder()
                    .email(email)
                    .password(passwordEncoder.encode(randomPassword))
                    .firstName(firstName)
                    .lastName(lastName)
                    .role(Role.USER)
                    .build();
            return userRepository.save(user);
        });
    }
}
