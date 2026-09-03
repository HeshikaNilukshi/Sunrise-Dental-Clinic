package com.sunrisedental.dental_clinic.service;

import com.sunrisedental.dental_clinic.dto.RegisterRequest;
import com.sunrisedental.dental_clinic.exception.DuplicateResourceException;
import com.sunrisedental.dental_clinic.model.User;
import com.sunrisedental.dental_clinic.model.enums.UserRole;
import com.sunrisedental.dental_clinic.repository.UserRepository;
import java.util.Optional;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, @Lazy PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User registerUser(RegisterRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new DuplicateResourceException("An account with email " + request.getEmail() + " already exists");
        }

        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match");
        }

        UserRole role = request.getRole() != null ? request.getRole() : UserRole.STAFF;
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        User user = new User(
                request.getFullName().trim(),
                normalizedEmail,
                encodedPassword,
                role
        );

        return userRepository.save(user);
    }

    public Optional<User> findByEmail(String email) {
        if (email == null) {
            return Optional.empty();
        }
        return userRepository.findByEmail(email.trim().toLowerCase());
    }
}
