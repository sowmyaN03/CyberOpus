package com.cyberopus.service;

import com.cyberopus.dto.request.LoginRequest;
import com.cyberopus.dto.request.RegisterRequest;
import com.cyberopus.dto.response.AuthResponse;
import com.cyberopus.dto.response.UserResponse;
import com.cyberopus.entity.Cart;
import com.cyberopus.entity.RewardPoints;
import com.cyberopus.entity.User;
import com.cyberopus.enums.Role;
import com.cyberopus.exception.BadRequestException;
import com.cyberopus.exception.DuplicateResourceException;
import com.cyberopus.repository.CartRepository;
import com.cyberopus.repository.RewardPointsRepository;
import com.cyberopus.repository.UserRepository;
import com.cyberopus.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final RewardPointsRepository rewardPointsRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already registered: " + request.getEmail());
        }

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .role(Role.CUSTOMER)
                .build();
        user = userRepository.save(user);

        // Create empty cart
        Cart cart = Cart.builder().user(user).build();
        cartRepository.save(cart);

        // Create reward points record
        RewardPoints rp = RewardPoints.builder().user(user).balance(0).build();
        rewardPointsRepository.save(rp);

        return toUserResponse(user);
    }

    public AuthResponse login(LoginRequest request) {
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
            UserDetails userDetails = (UserDetails) auth.getPrincipal();
            String token = jwtTokenProvider.generateToken(userDetails);

            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new BadRequestException("User not found"));

            return AuthResponse.builder()
                    .accessToken(token)
                    .tokenType("Bearer")
                    .expiresIn(jwtTokenProvider.getExpirationMs() / 1000)
                    .user(toUserResponse(user))
                    .build();
        } catch (BadCredentialsException e) {
            throw new BadRequestException("Invalid email or password");
        }
    }

    private UserResponse toUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
