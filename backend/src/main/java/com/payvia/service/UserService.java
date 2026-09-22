package com.payvia.service;

import com.payvia.dto.UserDto;
import com.payvia.dto.UserRegistrationDto;
import com.payvia.entity.User;
import com.payvia.entity.UserStatus;
import com.payvia.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final WalletService walletService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, WalletService walletService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.walletService = walletService;
    }


    @Transactional
    public UserDto registerUser(UserRegistrationDto registrationDto) {
        String normalizedEmail = registrationDto.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new IllegalArgumentException("Email is already registered");
        }

        User user = new User();
        user.setFullName(registrationDto.getFullName().trim());
        user.setEmail(normalizedEmail);
        user.setPasswordHash(passwordEncoder.encode(registrationDto.getPassword()));
        user.setStatus(UserStatus.ACTIVE);
        
        String baseName = registrationDto.getFullName().trim().toLowerCase().replaceAll("[^a-z0-9_]", "");
        if (baseName.isEmpty()) {
            baseName = "user";
        }
        String uniqueHandle = baseName + "@payvia";
        int counter = 1;
        while (userRepository.existsByPayviaHandle(uniqueHandle)) {
            uniqueHandle = baseName + counter + "@payvia";
            counter++;
        }
        user.setPayviaHandle(uniqueHandle);

        User savedUser = userRepository.save(user);
        walletService.createWalletForUser(savedUser);
        return mapToDto(savedUser);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email.trim().toLowerCase());
    }

    public UserDto mapToDto(User user) {
        return new UserDto(user.getId(), user.getFullName(), user.getEmail(), user.getPayviaHandle(), user.getStatus(), user.getRole());
    }
}
