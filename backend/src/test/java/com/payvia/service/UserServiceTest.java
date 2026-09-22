package com.payvia.service;

import com.payvia.dto.UserRegistrationDto;
import com.payvia.entity.User;
import com.payvia.entity.UserStatus;
import com.payvia.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.UUID;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class UserServiceTest {

    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private WalletService walletService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        userService = new UserService(userRepository, passwordEncoder, walletService);
    }

    @Test
    void testHandleGeneration_Unique() {
        UserRegistrationDto dto = new UserRegistrationDto();
        dto.setFullName("John Doe");
        dto.setEmail("john@example.com");
        dto.setPassword("pass");

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByPayviaHandle("johndoe@payvia")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(i -> {
            User u = i.getArgument(0);
            u.setId(UUID.randomUUID());
            return u;
        });

        userService.registerUser(dto);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        
        User savedUser = userCaptor.getValue();
        assertEquals("johndoe@payvia", savedUser.getPayviaHandle());
    }

    @Test
    void testHandleGeneration_DuplicateResolution() {
        UserRegistrationDto dto = new UserRegistrationDto();
        dto.setFullName("Jane Smith");
        dto.setEmail("jane@example.com");
        dto.setPassword("pass");

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByPayviaHandle("janesmith@payvia")).thenReturn(true);
        when(userRepository.existsByPayviaHandle("janesmith1@payvia")).thenReturn(true);
        when(userRepository.existsByPayviaHandle("janesmith2@payvia")).thenReturn(false);
        
        when(passwordEncoder.encode(anyString())).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(i -> {
            User u = i.getArgument(0);
            u.setId(UUID.randomUUID());
            return u;
        });

        userService.registerUser(dto);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        
        User savedUser = userCaptor.getValue();
        assertEquals("janesmith2@payvia", savedUser.getPayviaHandle());
    }
}

// touched to trigger IDE re-indexing



