package com.payvia.controller;

import com.payvia.dto.UserResolveDto;
import com.payvia.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/resolve/{handle}")
    public ResponseEntity<UserResolveDto> resolveUserByHandle(@PathVariable String handle) {
        return userRepository.findByPayviaHandle(handle)
                .map(user -> new UserResolveDto(user.getId(), user.getFullName(), user.getPayviaHandle()))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}

// touched to trigger IDE re-indexing

