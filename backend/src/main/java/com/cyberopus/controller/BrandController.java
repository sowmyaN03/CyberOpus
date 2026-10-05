package com.cyberopus.controller;

import com.cyberopus.dto.request.BrandRequest;
import com.cyberopus.dto.response.BookResponse;
import com.cyberopus.dto.response.BrandResponse;
import com.cyberopus.service.BookService;
import com.cyberopus.service.BrandService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/brands")
@RequiredArgsConstructor
@Tag(name = "Brands", description = "Publisher / brand endpoints")
public class BrandController {

    private final BrandService brandService;
    private final BookService bookService;

    @GetMapping
    @Operation(summary = "List all brands/publishers")
    public ResponseEntity<List<BrandResponse>> getAll() {
        return ResponseEntity.ok(brandService.getAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get brand by ID")
    public ResponseEntity<BrandResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(brandService.getById(id));
    }

    @GetMapping("/{id}/books")
    @Operation(summary = "List books by brand (paginated)")
    public ResponseEntity<Page<BookResponse>> getBooks(@PathVariable Long id,
                                                        @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(bookService.getByBrand(id, pageable));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create brand (ADMIN only)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<BrandResponse> create(@Valid @RequestBody BrandRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(brandService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update brand (ADMIN only)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<BrandResponse> update(@PathVariable Long id, @Valid @RequestBody BrandRequest request) {
        return ResponseEntity.ok(brandService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete brand (ADMIN only)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        brandService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
