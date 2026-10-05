package com.cyberopus.controller;

import com.cyberopus.dto.request.LoginRequest;
import com.cyberopus.dto.request.RegisterRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthControllerIT {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @Test
    void register_WithValidData_Returns201() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setFirstName("Test");
        req.setLastName("User");
        req.setEmail("testuser@example.com");
        req.setPassword("Password123!");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("testuser@example.com"))
                .andExpect(jsonPath("$.role").value("CUSTOMER"));
    }

    @Test
    void register_WithBlankEmail_Returns422() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setFirstName("Test");
        req.setLastName("User");
        req.setEmail("");
        req.setPassword("Password123!");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void register_WithInvalidEmail_Returns422() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setFirstName("Test");
        req.setLastName("User");
        req.setEmail("not-an-email");
        req.setPassword("Password123!");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.errors[0].field").value("email"));
    }

    @Test
    void register_WithShortPassword_Returns422() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setFirstName("Test");
        req.setLastName("User");
        req.setEmail("test2@example.com");
        req.setPassword("short");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void register_WithDuplicateEmail_Returns409() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setFirstName("Test");
        req.setLastName("User");
        req.setEmail("dup@example.com");
        req.setPassword("Password123!");

        // First registration
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());

        // Duplicate
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict());
    }

    @Test
    void login_WithValidCredentials_ReturnsToken() throws Exception {
        // First register
        RegisterRequest reg = new RegisterRequest();
        reg.setFirstName("Login");
        reg.setLastName("Test");
        reg.setEmail("logintest@example.com");
        reg.setPassword("Password123!");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reg)))
                .andExpect(status().isCreated());

        // Then login
        LoginRequest login = new LoginRequest();
        login.setEmail("logintest@example.com");
        login.setPassword("Password123!");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }

    @Test
    void login_WithBadCredentials_Returns400() throws Exception {
        LoginRequest login = new LoginRequest();
        login.setEmail("notexist@example.com");
        login.setPassword("WrongPass1!");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void protectedEndpoint_WithoutJwt_Returns401() throws Exception {
        mockMvc.perform(get("/api/cart"))
                .andExpect(status().isUnauthorized());
    }
}
