package com.example.bookstore.user.service;

import com.example.bookstore.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class UserAnalyticsService {

    private final UserRepository userRepository;

    public UserAnalyticsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public long countAllUsers() {
        return userRepository.count();
    }

    public long countCreatedBetween(LocalDateTime from, LocalDateTime to) {
        return userRepository.countCreatedBetween(from, to);
    }

    public List<LocalDateTime> findCreatedAtBetween(LocalDateTime from, LocalDateTime to) {
        return userRepository.findCreatedAtBetween(from, to);
    }
}

