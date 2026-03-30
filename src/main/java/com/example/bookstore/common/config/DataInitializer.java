package com.example.bookstore.common.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import com.example.bookstore.user.service.UserService;

@Component
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserService userService;

    public DataInitializer(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void run(String... args) throws Exception {
        initializeAdminUser();
        log.info("Data initialization completed");
    }

    private void initializeAdminUser() {
        userService.ensureDefaultAdminUser();
        log.info("Default admin user ensured (if database was empty): email=admin@bookstore.com");
    }
}
