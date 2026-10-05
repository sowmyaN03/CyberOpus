package com.cyberopus.service;

import com.cyberopus.dto.request.BookRequest;
import com.cyberopus.dto.response.BookResponse;
import com.cyberopus.entity.Book;
import com.cyberopus.entity.Brand;
import com.cyberopus.entity.Category;
import com.cyberopus.exception.ResourceNotFoundException;
import com.cyberopus.repository.BookRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock BookRepository bookRepository;
    @Mock CategoryService categoryService;
    @Mock BrandService brandService;

    @InjectMocks BookService bookService;

    private Category category() {
        return Category.builder().id(1L).name("Technology").build();
    }

    private Brand brand() {
        return Brand.builder().id(1L).name("O'Reilly").build();
    }

    private Book sampleBook() {
        return Book.builder()
                .id(1L)
                .title("Clean Code")
                .author("Robert C. Martin")
                .price(new BigDecimal("34.99"))
                .stockQuantity(10)
                .available(true)
                .category(category())
                .brand(brand())
                .build();
    }

    @Test
    void getAll_ReturnsPageOfBooks() {
        Page<Book> page = new PageImpl<>(List.of(sampleBook()));
        when(bookRepository.findByAvailableTrue(any())).thenReturn(page);
        when(categoryService.toResponse(any())).thenReturn(null);
        when(brandService.toResponse(any())).thenReturn(null);

        Page<BookResponse> result = bookService.getAll(PageRequest.of(0, 20));

        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    void getById_WhenExists_ReturnsBook() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(sampleBook()));
        when(categoryService.toResponse(any())).thenReturn(null);
        when(brandService.toResponse(any())).thenReturn(null);

        BookResponse resp = bookService.getById(1L);

        assertThat(resp.getTitle()).isEqualTo("Clean Code");
        assertThat(resp.getPrice()).isEqualByComparingTo("34.99");
    }

    @Test
    void getById_WhenNotExists_ThrowsResourceNotFoundException() {
        when(bookRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void create_BookWithValidData_ReturnsBookResponse() {
        BookRequest req = new BookRequest();
        req.setTitle("Test Book");
        req.setAuthor("Test Author");
        req.setPrice(new BigDecimal("19.99"));
        req.setCategoryId(1L);
        req.setBrandId(1L);
        req.setStockQuantity(10);
        req.setAvailable(true);

        when(categoryService.findById(1L)).thenReturn(category());
        when(brandService.findById(1L)).thenReturn(brand());
        when(bookRepository.save(any())).thenReturn(sampleBook());
        when(categoryService.toResponse(any())).thenReturn(null);
        when(brandService.toResponse(any())).thenReturn(null);

        BookResponse resp = bookService.create(req);
        assertThat(resp).isNotNull();
        assertThat(resp.getTitle()).isEqualTo("Clean Code");
    }
}
