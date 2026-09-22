package com.payvia;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.payvia.dto.LoginRequestDto;
import com.payvia.dto.UserRegistrationDto;
import com.payvia.entity.User;
import com.payvia.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
public class AuthIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity()).build();
        userRepository.deleteAll();
    }

    @Test
    void testRegistrationSuccess() throws Exception {
        UserRegistrationDto dto = new UserRegistrationDto();
        dto.setFullName("John Doe");
        dto.setEmail("john@example.com");
        dto.setPassword("password123");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fullName").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john@example.com"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());

        Optional<User> user = userRepository.findByEmail("john@example.com");
        assertTrue(user.isPresent());
        assertTrue(passwordEncoder.matches("password123", user.get().getPasswordHash()));
    }

    @Test
    void testRegistrationDuplicateEmail() throws Exception {
        UserRegistrationDto dto = new UserRegistrationDto();
        dto.setFullName("John Doe");
        dto.setEmail("john@example.com");
        dto.setPassword("password123");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("EMAIL_ALREADY_EXISTS"));
    }

    @Test
    void testInvalidEmail() throws Exception {
        UserRegistrationDto dto = new UserRegistrationDto();
        dto.setFullName("John Doe");
        dto.setEmail("invalid-email");
        dto.setPassword("password123");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    @Test
    void testLoginSuccess() throws Exception {
        UserRegistrationDto regDto = new UserRegistrationDto();
        regDto.setFullName("Jane Doe");
        regDto.setEmail("jane@example.com");
        regDto.setPassword("securepass");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(regDto)))
                .andExpect(status().isCreated());

        LoginRequestDto loginDto = new LoginRequestDto();
        loginDto.setEmail("jane@example.com");
        loginDto.setPassword("securepass");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isOk())
                .andExpect(cookie().exists("jwt"))
                .andExpect(cookie().httpOnly("jwt", true));
    }

    @Test
    void testLoginWrongPassword() throws Exception {
        UserRegistrationDto regDto = new UserRegistrationDto();
        regDto.setFullName("Jane Doe");
        regDto.setEmail("jane2@example.com");
        regDto.setPassword("securepass");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(regDto)))
                .andExpect(status().isCreated());

        LoginRequestDto loginDto = new LoginRequestDto();
        loginDto.setEmail("jane2@example.com");
        loginDto.setPassword("wrongpass");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("INVALID_CREDENTIALS"));
    }

    @Test
    void testMeUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testMeAuthenticated() throws Exception {
        UserRegistrationDto regDto = new UserRegistrationDto();
        regDto.setFullName("Auth User");
        regDto.setEmail("auth@example.com");
        regDto.setPassword("authpass123");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(regDto)))
                .andExpect(status().isCreated());

        LoginRequestDto loginDto = new LoginRequestDto();
        loginDto.setEmail("auth@example.com");
        loginDto.setPassword("authpass123");

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isOk())
                .andReturn();

        Cookie jwtCookie = loginResult.getResponse().getCookie("jwt");
        assertNotNull(jwtCookie);

        mockMvc.perform(get("/api/auth/me")
                .cookie(jwtCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("auth@example.com"))
                .andExpect(jsonPath("$.fullName").value("Auth User"));
    }

    @Test
    void testLogout() throws Exception {
        User user = new User();
        user.setFullName("Logout User");
        user.setEmail("logout@example.com");
        user.setPasswordHash(passwordEncoder.encode("password"));
        user.setPayviaHandle("logout");
        user.setStatus(com.payvia.entity.UserStatus.ACTIVE);
        userRepository.save(user);

        LoginRequestDto loginDto = new LoginRequestDto();
        loginDto.setEmail("logout@example.com");
        loginDto.setPassword("password");

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isOk())
                .andReturn();

        Cookie jwtCookie = loginResult.getResponse().getCookie("jwt");

        mockMvc.perform(post("/api/auth/logout")
                .cookie(jwtCookie))
                .andExpect(status().isOk())
                .andExpect(cookie().exists("jwt"))
                .andExpect(cookie().maxAge("jwt", 0));
    }
}

// touched to trigger IDE re-indexing


