package com.cyberopus.service;

import com.cyberopus.dto.request.RegisterRequest;
import com.cyberopus.dto.response.UserResponse;
import com.cyberopus.entity.Cart;
import com.cyberopus.entity.RewardPoints;
import com.cyberopus.entity.User;
import com.cyberopus.enums.Role;
import com.cyberopus.exception.DuplicateResourceException;
import com.cyberopus.repository.CartRepository;
import com.cyberopus.repository.RewardPointsRepository;
import com.cyberopus.repository.UserRepository;
import com.cyberopus.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock UserRepository userRepository;
    @Mock CartRepository cartRepository;
    @Mock RewardPointsRepository rewardPointsRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock AuthenticationManager authenticationManager;
    @Mock JwtTokenProvider jwtTokenProvider;

    @InjectMocks AuthService authService;

    private RegisterRequest validRequest;

    @BeforeEach
    void setUp() {
        validRequest = new RegisterRequest();
        validRequest.setFirstName("Alice");
        validRequest.setLastName("Demo");
        validRequest.setEmail("alice@demo.com");
        validRequest.setPassword("Password123!");
    }

    @Test
    void register_WithValidRequest_ReturnsUserResponse() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$12$hashed");

        User savedUser = User.builder()
                .id(1L)
                .firstName("Alice")
                .lastName("Demo")
                .email("alice@demo.com")
                .passwordHash("$2a$12$hashed")
                .role(Role.CUSTOMER)
                .createdAt(Instant.now())
                .build();
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(cartRepository.save(any(Cart.class))).thenReturn(new Cart());
        when(rewardPointsRepository.save(any(RewardPoints.class))).thenReturn(new RewardPoints());

        UserResponse result = authService.register(validRequest);

        assertThat(result.getEmail()).isEqualTo("alice@demo.com");
        assertThat(result.getRole()).isEqualTo(Role.CUSTOMER);
        verify(passwordEncoder).encode("Password123!");
        verify(cartRepository).save(any(Cart.class));
        verify(rewardPointsRepository).save(any(RewardPoints.class));
    }

    @Test
    void register_WithDuplicateEmail_ThrowsDuplicateResourceException() {
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        assertThatThrownBy(() -> authService.register(validRequest))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Email already registered");
    }
}
