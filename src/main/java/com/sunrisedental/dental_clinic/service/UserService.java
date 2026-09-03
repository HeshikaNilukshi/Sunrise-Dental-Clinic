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

    @Transactional(readOnly = true)
    public java.util.List<com.sunrisedental.dental_clinic.dto.StaffResponse> getAllStaff() {
        return userRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(java.util.stream.Collectors.toList());
    }

    @Transactional(readOnly = true)
    public com.sunrisedental.dental_clinic.dto.StaffResponse getStaffById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new com.sunrisedental.dental_clinic.exception.ResourceNotFoundException("Staff not found with id: " + id));
        return mapToResponse(user);
    }

    @Transactional
    public com.sunrisedental.dental_clinic.dto.StaffResponse updateStaff(Long id, com.sunrisedental.dental_clinic.dto.UpdateStaffRequest request, String currentUsername) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new com.sunrisedental.dental_clinic.exception.ResourceNotFoundException("Staff not found with id: " + id));

        // Security check: Only the owner admin can update their own admin account.
        if (user.getRole() == UserRole.ADMIN && !user.getEmail().equalsIgnoreCase(currentUsername)) {
            throw new org.springframework.security.access.AccessDeniedException("You cannot modify another Admin's account.");
        }

        String normalizedEmail = request.getEmail().trim().toLowerCase();
        if (!user.getEmail().equals(normalizedEmail) && userRepository.existsByEmail(normalizedEmail)) {
            throw new DuplicateResourceException("An account with email " + request.getEmail() + " already exists");
        }

        user.setFullName(request.getFullName().trim());
        user.setEmail(normalizedEmail);
        
        // Prevent an admin from demoting themselves by accident, or updating another's role
        if (user.getRole() == UserRole.ADMIN && request.getRole() != UserRole.ADMIN) {
             throw new IllegalArgumentException("Admin role cannot be changed.");
        }
        
        user.setRole(request.getRole());

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            if (!request.getPassword().equals(request.getConfirmPassword())) {
                throw new IllegalArgumentException("Passwords do not match");
            }
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        return mapToResponse(userRepository.save(user));
    }

    @Transactional
    public void deleteStaff(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new com.sunrisedental.dental_clinic.exception.ResourceNotFoundException("Staff not found with id: " + id));

        if (user.getRole() == UserRole.ADMIN) {
            throw new org.springframework.security.access.AccessDeniedException("Admin accounts cannot be deleted.");
        }

        userRepository.delete(user);
    }

    private com.sunrisedental.dental_clinic.dto.StaffResponse mapToResponse(User user) {
        return new com.sunrisedental.dental_clinic.dto.StaffResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole()
        );
    }
}
