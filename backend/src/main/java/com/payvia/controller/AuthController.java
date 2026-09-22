package com.payvia.controller;

import com.payvia.dto.LoginRequestDto;
import com.payvia.dto.UserDto;
import com.payvia.dto.UserRegistrationDto;
import com.payvia.entity.User;
import com.payvia.security.JwtUtil;
import com.payvia.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthController(UserService userService, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/register")
    public ResponseEntity<UserDto> register(@Valid @RequestBody UserRegistrationDto dto) {
        try {
            UserDto userDto = userService.registerUser(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(userDto);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "EMAIL_ALREADY_EXISTS");
        }
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@Valid @RequestBody LoginRequestDto dto, HttpServletResponse response) {
        Optional<User> userOpt = userService.findByEmail(dto.getEmail());
        if (userOpt.isEmpty() || !passwordEncoder.matches(dto.getPassword(), userOpt.get().getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS");
        }

        User user = userOpt.get();
        String token = jwtUtil.generateToken(user.getId(), user.getEmail(), user.getRole().name());

        Cookie cookie = new Cookie("jwt", token);
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // Should be true in production HTTPS
        cookie.setPath("/");
        cookie.setMaxAge(86400); // 1 day
        cookie.setAttribute("SameSite", "Lax");
        response.addCookie(cookie);

        return ResponseEntity.ok(Map.of("message", "Login successful"));
    }

    @GetMapping("/me")
    public ResponseEntity<UserDto> getCurrentUser(Principal principal) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED");
        }
        Optional<User> userOpt = userService.findByEmail(principal.getName());
        return userOpt.map(user -> ResponseEntity.ok(userService.mapToDto(user)))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED"));
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(HttpServletResponse response) {
        Cookie cookie = new Cookie("jwt", null);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        cookie.setAttribute("SameSite", "Lax");
        response.addCookie(cookie);

        return ResponseEntity.ok(Map.of("message", "Logout successful"));
    }
}
