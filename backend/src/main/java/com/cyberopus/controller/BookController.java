package com.cyberopus.controller;

import com.cyberopus.dto.request.BookRequest;
import com.cyberopus.dto.response.BookResponse;
import com.cyberopus.service.BookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
@Tag(name = "Books", description = "Book catalogue endpoints")
public class BookController {

    private final BookService bookService;

    @GetMapping
    @Operation(summary = "List all available books (paginated)")
    public ResponseEntity<Page<BookResponse>> getAll(
            @PageableDefault(size = 20, sort = "title", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(bookService.getAll(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get book details by ID")
    public ResponseEntity<BookResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(bookService.getById(id));
    }

    @GetMapping("/search")
    @Operation(summary = "Search books by keyword, category, brand, and price range")
    public ResponseEntity<Page<BookResponse>> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long brandId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(bookService.search(q, categoryId, brandId, minPrice, maxPrice, pageable));
    }

    @GetMapping("/{id}/related")
    @Operation(summary = "Get related books (same category, up to 8)")
    public ResponseEntity<List<BookResponse>> getRelated(@PathVariable Long id) {
        return ResponseEntity.ok(bookService.getRelated(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a book (ADMIN only)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<BookResponse> create(@Valid @RequestBody BookRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bookService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update a book (ADMIN only)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<BookResponse> update(@PathVariable Long id, @Valid @RequestBody BookRequest request) {
        return ResponseEntity.ok(bookService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a book (ADMIN only)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        bookService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
