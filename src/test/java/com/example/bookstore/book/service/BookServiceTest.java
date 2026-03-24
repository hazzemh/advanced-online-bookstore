package com.example.bookstore.book.service;

import com.example.bookstore.book.dto.BookResponse;
import com.example.bookstore.book.dto.CreateBookRequest;
import com.example.bookstore.book.dto.UpdateBookRequest;
import com.example.bookstore.book.entity.Book;
import com.example.bookstore.book.repository.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class BookServiceTest {

    @Autowired
    private BookService bookService;

    @Autowired
    private BookRepository bookRepository;

    private CreateBookRequest validCreateRequest;
    private UpdateBookRequest validUpdateRequest;
    private String suffix;

    @BeforeEach
    void setUp() {
        suffix = UUID.randomUUID().toString().substring(0, 8);

        validCreateRequest = new CreateBookRequest(
                "The Great Gatsby " + suffix,
                "F. Scott Fitzgerald",
                "A classic American novel",
                new BigDecimal("12.99"),
                50,
                "978-0-7432-7356-5-" + suffix,
                "Fiction",
                1925,
                180,
                "Scribner"
        );

        validUpdateRequest = new UpdateBookRequest(
                "The Great Gatsby - Updated " + suffix,
                "F. Scott Fitzgerald",
                "A classic American novel - Updated",
                new BigDecimal("14.99"),
                60,
                "978-0-7432-7356-5-" + suffix,
                "Fiction",
                1925,
                180,
                "Scribner"
        );
    }

    @Test
    void testCreateBook_Success() {
        BookResponse response = bookService.createBook(validCreateRequest);

        assertNotNull(response.id());
        assertEquals("The Great Gatsby " + suffix, response.title());
        assertEquals("F. Scott Fitzgerald", response.author());
        assertEquals(new BigDecimal("12.99"), response.price());
        assertEquals(50, response.stockQuantity());
    }

    @Test
    void testCreateBook_DuplicateIsbn() {
        bookService.createBook(validCreateRequest);

        assertThrows(RuntimeException.class, () -> {
            bookService.createBook(validCreateRequest);
        });
    }

    @Test
    void testGetBookById_Success() {
        BookResponse created = bookService.createBook(validCreateRequest);

        BookResponse retrieved = bookService.getBookById(created.id());

        assertNotNull(retrieved);
        assertEquals(created.id(), retrieved.id());
        assertEquals(created.title(), retrieved.title());
    }

    @Test
    void testGetBookById_NotFound() {
        assertThrows(RuntimeException.class, () -> {
            bookService.getBookById(UUID.randomUUID());
        });
    }

    @Test
    void testUpdateBook_Success() {
        BookResponse created = bookService.createBook(validCreateRequest);

        BookResponse updated = bookService.updateBook(created.id(), validUpdateRequest);

        assertEquals("The Great Gatsby - Updated " + suffix, updated.title());
        assertEquals(new BigDecimal("14.99"), updated.price());
        assertEquals(60, updated.stockQuantity());
    }

    @Test
    void testUpdateBook_NotFound() {
        assertThrows(RuntimeException.class, () -> {
            bookService.updateBook(UUID.randomUUID(), validUpdateRequest);
        });
    }

    @Test
    void testDeleteBook_Success() {
        BookResponse created = bookService.createBook(validCreateRequest);

        bookService.deleteBook(created.id());

        Book deleted = bookRepository.findById(created.id()).orElse(null);
        assertNotNull(deleted);
        assertFalse(deleted.getIsActive());
    }

    @Test
    void testSearchByTitle_Success() {
        bookService.createBook(validCreateRequest);

        Pageable pageable = PageRequest.of(0, 10);
        Page<BookResponse> results = bookService.searchByTitle("Gatsby " + suffix, pageable);

        assertTrue(results.getContent().size() > 0);
        assertEquals("The Great Gatsby " + suffix, results.getContent().get(0).title());
    }

    @Test
    void testSearchByAuthor_Success() {
        bookService.createBook(validCreateRequest);

        Pageable pageable = PageRequest.of(0, 10);
        Page<BookResponse> results = bookService.searchByAuthor("Fitzgerald", pageable);

        assertTrue(results.getContent().size() > 0);
        assertEquals("F. Scott Fitzgerald", results.getContent().get(0).author());
    }

    @Test
    void testFindByGenre_Success() {
        bookService.createBook(validCreateRequest);

        Pageable pageable = PageRequest.of(0, 10);
        Page<BookResponse> results = bookService.findByGenre("Fiction", pageable);

        assertTrue(results.getContent().size() > 0);
        assertEquals("Fiction", results.getContent().get(0).genre());
    }

    @Test
    void testFindByPriceRange_Success() {
        bookService.createBook(validCreateRequest);

        Pageable pageable = PageRequest.of(0, 10);
        Page<BookResponse> results = bookService.findByPriceRange(
                new BigDecimal("10.00"),
                new BigDecimal("15.00"),
                pageable
        );

        assertTrue(results.getContent().size() > 0);
    }

    @Test
    void testFindAvailableBooks_Success() {
        bookService.createBook(validCreateRequest);

        Pageable pageable = PageRequest.of(0, 10);
        Page<BookResponse> results = bookService.findAvailableBooks(pageable);

        assertTrue(results.getContent().size() > 0);
        assertTrue(results.getContent().get(0).stockQuantity() > 0);
    }

    @Test
    void testGetAllBooks_Success() {
        bookService.createBook(validCreateRequest);

        Pageable pageable = PageRequest.of(0, 10);
        Page<BookResponse> results = bookService.getAllBooks(pageable);

        assertTrue(results.getTotalElements() >= 1);
    }
}

