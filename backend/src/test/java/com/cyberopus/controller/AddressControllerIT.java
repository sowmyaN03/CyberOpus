package com.cyberopus.controller;

import com.cyberopus.dto.request.AddressRequest;
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
class AddressControllerIT {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    private String jwtAlice;
    private String jwtBob;

    private String registerAndLogin(String email) throws Exception {
        RegisterRequest reg = new RegisterRequest();
        reg.setFirstName("Test");
        reg.setLastName("User");
        reg.setEmail(email);
        reg.setPassword("Password123!");
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reg)));

        LoginRequest login = new LoginRequest();
        login.setEmail(email);
        login.setPassword("Password123!");
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(login))).andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString())
                .get("accessToken").asText();
    }

    @BeforeEach
    void setUp() throws Exception {
        jwtAlice = registerAndLogin("addr_alice@test.com");
        jwtBob   = registerAndLogin("addr_bob@test.com");
    }

    private AddressRequest validAddress() {
        AddressRequest req = new AddressRequest();
        req.setFullName("Alice Test");
        req.setLine1("123 Test St");
        req.setCity("Testville");
        req.setState("CA");
        req.setPostalCode("94105");
        req.setCountry("US");
        return req;
    }

    @Test
    void createAddress_ValidRequest_Returns201() throws Exception {
        mockMvc.perform(post("/api/addresses")
                        .header("Authorization", "Bearer " + jwtAlice)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validAddress())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.city").value("Testville"));
    }

    @Test
    void createAddress_BlankFullName_Returns422() throws Exception {
        AddressRequest req = validAddress();
        req.setFullName("");

        mockMvc.perform(post("/api/addresses")
                        .header("Authorization", "Bearer " + jwtAlice)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void deleteAddress_OtherUserAddress_Returns403or404() throws Exception {
        // Alice creates an address
        MvcResult result = mockMvc.perform(post("/api/addresses")
                        .header("Authorization", "Bearer " + jwtAlice)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validAddress())))
                .andReturn();
        Long addressId = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        // Bob tries to delete Alice's address
        mockMvc.perform(delete("/api/addresses/" + addressId)
                        .header("Authorization", "Bearer " + jwtBob))
                .andExpect(result2 -> {
                    int status = result2.getResponse().getStatus();
                    assert status == 403 || status == 404 : "Expected 403 or 404 but got " + status;
                });
    }
}
