package com.cyberopus.service;

import com.cyberopus.dto.request.AddressRequest;
import com.cyberopus.dto.response.AddressResponse;
import com.cyberopus.entity.Address;
import com.cyberopus.entity.User;
import com.cyberopus.exception.ForbiddenException;
import com.cyberopus.exception.ResourceNotFoundException;
import com.cyberopus.repository.AddressRepository;
import com.cyberopus.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AddressService {

    private final AddressRepository addressRepository;
    private final SecurityUtils securityUtils;

    public List<AddressResponse> getMyAddresses() {
        Long userId = securityUtils.getCurrentUserId();
        return addressRepository.findByUserId(userId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public AddressResponse create(AddressRequest request) {
        User user = securityUtils.getCurrentUser();

        if (Boolean.TRUE.equals(request.getIsDefault())) {
            addressRepository.clearDefaultForUser(user.getId());
        }

        Address address = Address.builder()
                .user(user)
                .fullName(request.getFullName())
                .line1(request.getLine1())
                .line2(request.getLine2())
                .city(request.getCity())
                .state(request.getState())
                .postalCode(request.getPostalCode())
                .country(request.getCountry())
                .phone(request.getPhone())
                .isDefault(Boolean.TRUE.equals(request.getIsDefault()))
                .build();
        return toResponse(addressRepository.save(address));
    }

    @Transactional
    public AddressResponse update(Long id, AddressRequest request) {
        Address address = findOwnedById(id);

        if (Boolean.TRUE.equals(request.getIsDefault())) {
            addressRepository.clearDefaultForUser(address.getUser().getId());
        }

        address.setFullName(request.getFullName());
        address.setLine1(request.getLine1());
        address.setLine2(request.getLine2());
        address.setCity(request.getCity());
        address.setState(request.getState());
        address.setPostalCode(request.getPostalCode());
        address.setCountry(request.getCountry());
        address.setPhone(request.getPhone());
        address.setIsDefault(Boolean.TRUE.equals(request.getIsDefault()));
        return toResponse(addressRepository.save(address));
    }

    @Transactional
    public void delete(Long id) {
        findOwnedById(id);
        addressRepository.deleteById(id);
    }

    @Transactional
    public AddressResponse setDefault(Long id) {
        Address address = findOwnedById(id);
        addressRepository.clearDefaultForUser(address.getUser().getId());
        address.setIsDefault(true);
        return toResponse(addressRepository.save(address));
    }

    /** Used by CheckoutService to validate address belongs to the user */
    public Address findOwnedByIdForUser(Long id, Long userId) {
        return addressRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found: " + id));
    }

    private Address findOwnedById(Long id) {
        Long userId = securityUtils.getCurrentUserId();
        Address address = addressRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found: " + id));
        if (!address.getUser().getId().equals(userId)) {
            throw new ForbiddenException("You do not own this address");
        }
        return address;
    }

    public AddressResponse toResponse(Address a) {
        return AddressResponse.builder()
                .id(a.getId())
                .fullName(a.getFullName())
                .line1(a.getLine1())
                .line2(a.getLine2())
                .city(a.getCity())
                .state(a.getState())
                .postalCode(a.getPostalCode())
                .country(a.getCountry())
                .phone(a.getPhone())
                .isDefault(a.getIsDefault())
                .createdAt(a.getCreatedAt())
                .build();
    }
}
