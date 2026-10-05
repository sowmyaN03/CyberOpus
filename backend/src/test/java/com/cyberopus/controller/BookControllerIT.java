package com.cyberopus.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class BookControllerIT {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @Test
    void getBooks_PublicEndpoint_Returns200() throws Exception {
        mockMvc.perform(get("/api/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void getBooks_WithoutAuth_Returns200() throws Exception {
        // Public read access – no auth needed
        mockMvc.perform(get("/api/books"))
                .andExpect(status().isOk());
    }

    @Test
    void getBookById_NonExistent_Returns404() throws Exception {
        mockMvc.perform(get("/api/books/999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void searchBooks_WithQuery_Returns200() throws Exception {
        mockMvc.perform(get("/api/books/search").param("q", "java"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void getCategories_PublicEndpoint_Returns200() throws Exception {
        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getBrands_PublicEndpoint_Returns200() throws Exception {
        mockMvc.perform(get("/api/brands"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void createBook_WithoutAuth_Returns401() throws Exception {
        mockMvc.perform(post("/api/books")
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }
}
