package com.example.bookstore.common.config;

import com.example.bookstore.user.entity.Role;
import com.example.bookstore.user.entity.User;
import com.example.bookstore.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        initializeAdminUser();
        log.info("Data initialization completed");
    }

    private void initializeAdminUser() {
        if (userRepository.count() == 0) {
            User adminUser = User.builder()
                    .email("admin@bookstore.com")
                    .password(passwordEncoder.encode("AdminPassword123!"))
                    .firstName("System")
                    .lastName("Administrator")
                    .role(Role.ADMIN)
                    .build();

            userRepository.save(adminUser);
            log.info("Default admin user created:");
            log.info("Email: admin@bookstore.com");
            log.info("Password: AdminPassword123!");
            log.info("Please change the password after first login!");
        } else {
            log.info("Admin user already exists, skipping initialization");
        }
    }
}
