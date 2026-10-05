package com.cyberopus.controller;

import com.cyberopus.dto.request.AddressRequest;
import com.cyberopus.dto.response.AddressResponse;
import com.cyberopus.service.AddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/addresses")
@RequiredArgsConstructor
@Tag(name = "Addresses", description = "Delivery address management")
@SecurityRequirement(name = "bearerAuth")
public class AddressController {

    private final AddressService addressService;

    @GetMapping
    @Operation(summary = "List all delivery addresses for the current user")
    public ResponseEntity<List<AddressResponse>> getAll() {
        return ResponseEntity.ok(addressService.getMyAddresses());
    }

    @PostMapping
    @Operation(summary = "Add a new delivery address")
    public ResponseEntity<AddressResponse> create(@Valid @RequestBody AddressRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(addressService.create(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing address")
    public ResponseEntity<AddressResponse> update(@PathVariable Long id, @Valid @RequestBody AddressRequest request) {
        return ResponseEntity.ok(addressService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an address")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        addressService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/default")
    @Operation(summary = "Set address as default")
    public ResponseEntity<AddressResponse> setDefault(@PathVariable Long id) {
        return ResponseEntity.ok(addressService.setDefault(id));
    }
}
