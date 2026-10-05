package com.cyberopus.service;

import com.cyberopus.dto.request.BookRequest;
import com.cyberopus.dto.response.BookResponse;
import com.cyberopus.dto.response.CategoryResponse;
import com.cyberopus.dto.response.BrandResponse;
import com.cyberopus.entity.Book;
import com.cyberopus.entity.Category;
import com.cyberopus.entity.Brand;
import com.cyberopus.exception.DuplicateResourceException;
import com.cyberopus.exception.ResourceNotFoundException;
import com.cyberopus.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;
    private final CategoryService categoryService;
    private final BrandService brandService;

    @Value("${app.delivery.business-days:5}")
    private int deliveryBusinessDays;

    public Page<BookResponse> getAll(Pageable pageable) {
        return bookRepository.findByAvailableTrue(pageable).map(this::toResponse);
    }

    public BookResponse getById(Long id) {
        Book book = findEntityById(id);
        return toResponse(book);
    }

    public Page<BookResponse> search(String q, Long categoryId, Long brandId,
                                      BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {
        return bookRepository.search(q, categoryId, brandId, minPrice, maxPrice, pageable)
                .map(this::toResponse);
    }

    public Page<BookResponse> getByCategory(Long categoryId, Pageable pageable) {
        categoryService.findById(categoryId); // validates existence
        return bookRepository.findByCategoryIdAndAvailableTrue(categoryId, pageable).map(this::toResponse);
    }

    public Page<BookResponse> getByBrand(Long brandId, Pageable pageable) {
        brandService.findById(brandId); // validates existence
        return bookRepository.findByBrandIdAndAvailableTrue(brandId, pageable).map(this::toResponse);
    }

    public List<BookResponse> getRelated(Long bookId) {
        Book book = findEntityById(bookId);
        Pageable limit = PageRequest.of(0, 8);
        return bookRepository.findByCategoryIdAndAvailableTrueAndIdNot(
                book.getCategory().getId(), bookId, limit
        ).stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public BookResponse create(BookRequest request) {
        if (request.getIsbn() != null && !request.getIsbn().isBlank()
                && bookRepository.findAll().stream().anyMatch(b ->
                request.getIsbn().equals(b.getIsbn()))) {
            throw new DuplicateResourceException("ISBN already exists: " + request.getIsbn());
        }
        Category category = categoryService.findById(request.getCategoryId());
        Brand brand = brandService.findById(request.getBrandId());

        Book book = Book.builder()
                .title(request.getTitle())
                .author(request.getAuthor())
                .description(request.getDescription())
                .isbn(request.getIsbn())
                .price(request.getPrice())
                .stockQuantity(request.getStockQuantity())
                .imageUrl(request.getImageUrl())
                .category(category)
                .brand(brand)
                .available(request.getAvailable() != null ? request.getAvailable() : true)
                .build();
        return toResponse(bookRepository.save(book));
    }

    @Transactional
    public BookResponse update(Long id, BookRequest request) {
        Book book = findEntityById(id);
        Category category = categoryService.findById(request.getCategoryId());
        Brand brand = brandService.findById(request.getBrandId());

        book.setTitle(request.getTitle());
        book.setAuthor(request.getAuthor());
        book.setDescription(request.getDescription());
        book.setIsbn(request.getIsbn());
        book.setPrice(request.getPrice());
        book.setStockQuantity(request.getStockQuantity());
        book.setImageUrl(request.getImageUrl());
        book.setCategory(category);
        book.setBrand(brand);
        if (request.getAvailable() != null) {
            book.setAvailable(request.getAvailable());
        }
        return toResponse(bookRepository.save(book));
    }

    @Transactional
    public void delete(Long id) {
        findEntityById(id);
        bookRepository.deleteById(id);
    }

    public Book findEntityById(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found: " + id));
    }

    public BookResponse toResponse(Book book) {
        CategoryResponse catResp = categoryService.toResponse(book.getCategory());
        BrandResponse brandResp = brandService.toResponse(book.getBrand());
        return BookResponse.builder()
                .id(book.getId())
                .title(book.getTitle())
                .author(book.getAuthor())
                .description(book.getDescription())
                .isbn(book.getIsbn())
                .price(book.getPrice())
                .stockQuantity(book.getStockQuantity())
                .imageUrl(book.getImageUrl())
                .category(catResp)
                .brand(brandResp)
                .available(book.getAvailable())
                .createdAt(book.getCreatedAt())
                .updatedAt(book.getUpdatedAt())
                .tentativeDeliveryDate(computeDeliveryDate())
                .build();
    }

    /** Computes tentative delivery = today + N business days */
    private LocalDate computeDeliveryDate() {
        LocalDate date = LocalDate.now();
        int added = 0;
        while (added < deliveryBusinessDays) {
            date = date.plusDays(1);
            if (date.getDayOfWeek() != DayOfWeek.SATURDAY && date.getDayOfWeek() != DayOfWeek.SUNDAY) {
                added++;
            }
        }
        return date;
    }
}
