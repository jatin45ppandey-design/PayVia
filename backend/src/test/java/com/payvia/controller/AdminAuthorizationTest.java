package com.payvia.controller;

import com.payvia.entity.Role;
import com.payvia.entity.User;
import com.payvia.entity.UserStatus;
import com.payvia.repository.UserRepository;
import com.payvia.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class AdminAuthorizationTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    private String userToken;
    private String adminToken;

    @BeforeEach
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity()).build();
        
        User normalUser = new User();
        normalUser.setEmail("user@example.com");
        normalUser.setFullName("Normal User");
        normalUser.setPayviaHandle("normal");
        normalUser.setStatus(UserStatus.ACTIVE);
        normalUser.setPasswordHash("hash");
        normalUser.setRole(Role.USER);
        userRepository.save(normalUser);

        User adminUser = new User();
        adminUser.setEmail("admin@example.com");
        adminUser.setFullName("Admin User");
        adminUser.setPayviaHandle("admin");
        adminUser.setStatus(UserStatus.ACTIVE);
        adminUser.setPasswordHash("hash");
        adminUser.setRole(Role.ADMIN);
        userRepository.save(adminUser);

        userToken = jwtUtil.generateToken(normalUser.getId(), normalUser.getEmail(), normalUser.getRole().name());
        adminToken = jwtUtil.generateToken(adminUser.getId(), adminUser.getEmail(), adminUser.getRole().name());
    }

    @Test
    public void testAdminEndpoint_withoutToken_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/admin/metrics"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testAdminEndpoint_withUserToken_Forbidden() throws Exception {
        mockMvc.perform(get("/api/admin/metrics")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testAdminEndpoint_withAdminToken_Ok() throws Exception {
        mockMvc.perform(get("/api/admin/metrics")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }
}

