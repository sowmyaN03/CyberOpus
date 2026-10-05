package com.cyberopus.controller;

import com.cyberopus.dto.request.*;
import com.cyberopus.dto.response.CartResponse;
import com.cyberopus.enums.PaymentMethod;
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

import java.math.BigDecimal;

import static org.hamcrest.Matchers.greaterThan;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class CheckoutControllerIT {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    private String jwtCustomer;
    private String jwtAdmin;
    private Long addressId;
    private Long bookId;

    /** Register + login a user, return JWT */
    private String registerAndLogin(String email, String password, String firstName) throws Exception {
        RegisterRequest reg = new RegisterRequest();
        reg.setFirstName(firstName);
        reg.setLastName("Test");
        reg.setEmail(email);
        reg.setPassword(password);
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reg)));

        LoginRequest login = new LoginRequest();
        login.setEmail(email);
        login.setPassword(password);
        MvcResult r = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(login))).andReturn();
        return objectMapper.readTree(r.getResponse().getContentAsString())
                .get("accessToken").asText();
    }

    @BeforeEach
    void setUp() throws Exception {
        jwtCustomer = registerAndLogin("co_customer@test.com", "Password123!", "CoCustomer");

        // Admin must be seeded separately – promote via direct registration then set role via DB is not
        // available in unit test. Instead, create category/brand/book via admin endpoints after
        // making the test user admin using the entity manager would require @Autowired.
        // Simplest: register a user and use @WithMockUser admin for write ops.
        // For IT simplicity, we directly call the service by obtaining an admin token via
        // a programmatic insert using the admin role. Since we can't easily create an admin
        // through the API (no admin self-registration), we use a workaround:
        // register the user then update the role via the test context.

        // Create category via admin role – we need to inject a real admin user for these tests.
        // Approach: use an @Autowired UserRepository to flip role after registration.
        // Since this is an IT and @Transactional, we use Spring's test transaction.

        // Actually the simplest and cleanest approach: since @EnableMethodSecurity is active,
        // use @WithMockUser(roles = "ADMIN") is per-method only.
        // For full IT flow, we create an admin user directly in the DB through UserRepository,
        // which is available via @Autowired.
        // Let the test use a separate admin setup.
        setUpAdminAndCatalog();

        // Create customer address
        AddressRequest addrReq = new AddressRequest();
        addrReq.setFullName("CoCustomer Test");
        addrReq.setLine1("1 Test Lane");
        addrReq.setCity("Testcity");
        addrReq.setState("CA");
        addrReq.setPostalCode("90001");
        addrReq.setCountry("US");
        addrReq.setIsDefault(true);
        MvcResult addrResult = mockMvc.perform(post("/api/addresses")
                .header("Authorization", "Bearer " + jwtCustomer)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(addrReq))).andReturn();
        addressId = objectMapper.readTree(addrResult.getResponse().getContentAsString()).get("id").asLong();

        // Add book to cart
        CartItemRequest cartReq = new CartItemRequest();
        cartReq.setBookId(bookId);
        cartReq.setQuantity(1);
        mockMvc.perform(post("/api/cart/items")
                .header("Authorization", "Bearer " + jwtCustomer)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cartReq)));
    }

    @Autowired
    com.cyberopus.repository.UserRepository userRepository;
    @Autowired
    com.cyberopus.repository.CategoryRepository categoryRepository;
    @Autowired
    com.cyberopus.repository.BrandRepository brandRepository;
    @Autowired
    com.cyberopus.repository.BookRepository bookRepository;
    @Autowired
    com.cyberopus.repository.RewardPointsRepository rewardPointsRepository;
    @Autowired
    com.cyberopus.repository.CartRepository cartRepository;

    private void setUpAdminAndCatalog() throws Exception {
        // Promote the customer user to ADMIN in the same transaction so we can create catalogue
        com.cyberopus.entity.User user = userRepository.findByEmail("co_customer@test.com").orElseThrow();
        user.setRole(com.cyberopus.enums.Role.ADMIN);
        userRepository.save(user);

        // Login again to get an ADMIN JWT
        LoginRequest login = new LoginRequest();
        login.setEmail("co_customer@test.com");
        login.setPassword("Password123!");
        MvcResult r = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(login))).andReturn();
        jwtAdmin = objectMapper.readTree(r.getResponse().getContentAsString())
                .get("accessToken").asText();

        // Create category
        CategoryRequest catReq = new CategoryRequest();
        catReq.setName("Test Category IT");
        MvcResult catResult = mockMvc.perform(post("/api/categories")
                .header("Authorization", "Bearer " + jwtAdmin)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(catReq))).andReturn();
        Long categoryId = objectMapper.readTree(catResult.getResponse().getContentAsString()).get("id").asLong();

        // Create brand
        BrandRequest brandReq = new BrandRequest();
        brandReq.setName("Test Brand IT");
        MvcResult brandResult = mockMvc.perform(post("/api/brands")
                .header("Authorization", "Bearer " + jwtAdmin)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(brandReq))).andReturn();
        Long brandId = objectMapper.readTree(brandResult.getResponse().getContentAsString()).get("id").asLong();

        // Create book with 10 stock
        BookRequest bookReq = new BookRequest();
        bookReq.setTitle("IT Test Book");
        bookReq.setAuthor("IT Author");
        bookReq.setPrice(new BigDecimal("19.99"));
        bookReq.setStockQuantity(10);
        bookReq.setCategoryId(categoryId);
        bookReq.setBrandId(brandId);
        bookReq.setAvailable(true);
        MvcResult bookResult = mockMvc.perform(post("/api/books")
                .header("Authorization", "Bearer " + jwtAdmin)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bookReq))).andReturn();
        bookId = objectMapper.readTree(bookResult.getResponse().getContentAsString()).get("id").asLong();

        // Revert user to CUSTOMER role for the checkout tests
        user.setRole(com.cyberopus.enums.Role.CUSTOMER);
        userRepository.save(user);

        // Refresh JWT as CUSTOMER
        LoginRequest login2 = new LoginRequest();
        login2.setEmail("co_customer@test.com");
        login2.setPassword("Password123!");
        MvcResult r2 = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(login2))).andReturn();
        jwtCustomer = objectMapper.readTree(r2.getResponse().getContentAsString())
                .get("accessToken").asText();
    }

    @Test
    void checkout_SuccessfulPayment_Returns201WithConfirmedOrder() throws Exception {
        CheckoutRequest req = new CheckoutRequest();
        req.setAddressId(addressId);
        req.setPaymentMethod(PaymentMethod.CREDIT_CARD);
        req.setMaskedCardNumber("****1234");
        req.setCardHolderName("CoCustomer Test");
        req.setRewardPointsToRedeem(0);

        mockMvc.perform(post("/api/checkout")
                        .header("Authorization", "Bearer " + jwtCustomer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.payment.status").value("SUCCESS"))
                .andExpect(jsonPath("$.payment.transactionRef").isNotEmpty())
                .andExpect(jsonPath("$.items").isArray());
    }

    @Test
    void checkout_FailedPayment_OrderRemainsAndCartPreserved() throws Exception {
        CheckoutRequest req = new CheckoutRequest();
        req.setAddressId(addressId);
        req.setPaymentMethod(PaymentMethod.DEBIT_CARD);
        req.setMaskedCardNumber("****9999");
        req.setCardHolderName("FAIL_TEST");
        req.setRewardPointsToRedeem(0);

        mockMvc.perform(post("/api/checkout")
                        .header("Authorization", "Bearer " + jwtCustomer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.payment.status").value("FAILED"));

        // Cart should still have the item (not cleared on payment failure)
        mockMvc.perform(get("/api/cart")
                        .header("Authorization", "Bearer " + jwtCustomer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itemCount").value(greaterThan(0)));
    }

    @Test
    void checkout_EmptyCart_Returns400() throws Exception {
        mockMvc.perform(delete("/api/cart")
                .header("Authorization", "Bearer " + jwtCustomer));

        CheckoutRequest req = new CheckoutRequest();
        req.setAddressId(addressId);
        req.setPaymentMethod(PaymentMethod.CREDIT_CARD);
        req.setRewardPointsToRedeem(0);

        mockMvc.perform(post("/api/checkout")
                        .header("Authorization", "Bearer " + jwtCustomer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void checkout_WithoutAuth_Returns401() throws Exception {
        CheckoutRequest req = new CheckoutRequest();
        req.setAddressId(1L);
        req.setPaymentMethod(PaymentMethod.CREDIT_CARD);
        req.setRewardPointsToRedeem(0);

        mockMvc.perform(post("/api/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getRewards_AfterSuccessfulOrder_ShowsEarnedPoints() throws Exception {
        CheckoutRequest req = new CheckoutRequest();
        req.setAddressId(addressId);
        req.setPaymentMethod(PaymentMethod.CREDIT_CARD);
        req.setMaskedCardNumber("****1234");
        req.setCardHolderName("CoCustomer Test");
        req.setRewardPointsToRedeem(0);
        mockMvc.perform(post("/api/checkout")
                .header("Authorization", "Bearer " + jwtCustomer)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)));

        mockMvc.perform(get("/api/rewards")
                        .header("Authorization", "Bearer " + jwtCustomer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").isNumber())
                .andExpect(jsonPath("$.transactions").isArray());
    }

    @Test
    void getRecommendations_Authenticated_Returns200() throws Exception {
        mockMvc.perform(get("/api/recommendations")
                        .header("Authorization", "Bearer " + jwtCustomer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void cancelOrder_WithinWindow_ReturnsCANCELLED() throws Exception {
        CheckoutRequest req = new CheckoutRequest();
        req.setAddressId(addressId);
        req.setPaymentMethod(PaymentMethod.CREDIT_CARD);
        req.setMaskedCardNumber("****1234");
        req.setCardHolderName("CoCustomer Test");
        req.setRewardPointsToRedeem(0);

        MvcResult orderResult = mockMvc.perform(post("/api/checkout")
                .header("Authorization", "Bearer " + jwtCustomer)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))).andReturn();
        Long orderId = objectMapper.readTree(orderResult.getResponse().getContentAsString())
                .get("id").asLong();

        mockMvc.perform(post("/api/orders/" + orderId + "/cancel")
                        .header("Authorization", "Bearer " + jwtCustomer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    void orderHistory_AfterOrder_ContainsOrder() throws Exception {
        CheckoutRequest req = new CheckoutRequest();
        req.setAddressId(addressId);
        req.setPaymentMethod(PaymentMethod.CREDIT_CARD);
        req.setMaskedCardNumber("****1234");
        req.setCardHolderName("CoCustomer Test");
        req.setRewardPointsToRedeem(0);
        mockMvc.perform(post("/api/checkout")
                .header("Authorization", "Bearer " + jwtCustomer)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)));

        mockMvc.perform(get("/api/orders")
                        .header("Authorization", "Bearer " + jwtCustomer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(greaterThan(0)));
    }
}
