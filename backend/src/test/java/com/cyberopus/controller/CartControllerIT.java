package com.cyberopus.controller;

import com.cyberopus.dto.request.LoginRequest;
import com.cyberopus.dto.request.RegisterRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
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
class CartControllerIT {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    private String jwt;

    @BeforeEach
    void setUp() throws Exception {
        RegisterRequest reg = new RegisterRequest();
        reg.setFirstName("Cart");
        reg.setLastName("User");
        reg.setEmail("cartuser@test.com");
        reg.setPassword("Password123!");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reg)));

        LoginRequest login = new LoginRequest();
        login.setEmail("cartuser@test.com");
        login.setPassword("Password123!");

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andReturn();

        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        jwt = body.get("accessToken").asText();
    }

    @Test
    void getCart_Authenticated_Returns200() throws Exception {
        mockMvc.perform(get("/api/cart")
                        .header("Authorization", "Bearer " + jwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray());
    }

    @Test
    void getCart_NoAuth_Returns401() throws Exception {
        mockMvc.perform(get("/api/cart"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void addItem_WithInvalidQuantity_Returns422() throws Exception {
        String body = """
                {"bookId": 1, "quantity": 0}
                """;
        mockMvc.perform(post("/api/cart/items")
                        .header("Authorization", "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void addItem_NonExistentBook_Returns404() throws Exception {
        String body = """
                {"bookId": 999999, "quantity": 1}
                """;
        mockMvc.perform(post("/api/cart/items")
                        .header("Authorization", "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }

    @Test
    void clearCart_Authenticated_Returns204() throws Exception {
        mockMvc.perform(delete("/api/cart")
                        .header("Authorization", "Bearer " + jwt))
                .andExpect(status().isNoContent());
    }
}
