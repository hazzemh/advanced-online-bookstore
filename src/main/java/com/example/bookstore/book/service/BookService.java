package com.example.bookstore.book.service;

import com.example.bookstore.book.dto.BookResponse;
import com.example.bookstore.book.dto.CreateBookRequest;
import com.example.bookstore.book.dto.UpdateBookRequest;
import com.example.bookstore.book.entity.Book;
import com.example.bookstore.book.repository.BookRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.UUID;

@Service
public class BookService {

    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    // Admin Operations
    public BookResponse createBook(CreateBookRequest request) {
        if (bookRepository.findByIsbn(request.isbn()).isPresent()) {
            throw new RuntimeException("Book with ISBN " + request.isbn() + " already exists");
        }

        Book book = Book.builder()
                .title(request.title())
                .author(request.author())
                .description(request.description())
                .price(request.price())
                .stockQuantity(request.stockQuantity())
                .isbn(request.isbn())
                .genre(request.genre())
                .publicationYear(request.publicationYear())
                .pages(request.pages())
                .publisher(request.publisher())
                .build();

        Book savedBook = bookRepository.save(book);
        return mapToResponse(savedBook);
    }

    public BookResponse updateBook(UUID bookId, UpdateBookRequest request) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found"));

        book.setTitle(request.title());
        book.setAuthor(request.author());
        book.setDescription(request.description());
        book.setPrice(request.price());
        book.setStockQuantity(request.stockQuantity());
        book.setIsbn(request.isbn());
        book.setGenre(request.genre());
        book.setPublicationYear(request.publicationYear());
        book.setPages(request.pages());
        book.setPublisher(request.publisher());

        Book updatedBook = bookRepository.save(book);
        return mapToResponse(updatedBook);
    }

    public void deleteBook(UUID bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found"));
        book.setIsActive(false);
        bookRepository.save(book);
    }

    // Public Operations
    public BookResponse getBookById(UUID bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found"));
        return mapToResponse(book);
    }

    public Page<BookResponse> getAllBooks(Pageable pageable) {
        return bookRepository.findByIsActive(true, pageable)
                .map(this::mapToResponse);
    }

    public Page<BookResponse> searchByTitle(String keyword, Pageable pageable) {
        return bookRepository.searchByTitle(keyword, pageable)
                .map(this::mapToResponse);
    }

    public Page<BookResponse> searchByAuthor(String keyword, Pageable pageable) {
        return bookRepository.searchByAuthor(keyword, pageable)
                .map(this::mapToResponse);
    }

    public Page<BookResponse> findByGenre(String genre, Pageable pageable) {
        return bookRepository.findByGenre(genre, pageable)
                .map(this::mapToResponse);
    }

    public Page<BookResponse> findByPriceRange(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {
        return bookRepository.findByPriceRange(minPrice, maxPrice, pageable)
                .map(this::mapToResponse);
    }

    public Page<BookResponse> findTopRatedBooks(Pageable pageable) {
        return bookRepository.findTopRatedBooks(pageable)
                .map(this::mapToResponse);
    }

    public Page<BookResponse> findAvailableBooks(Pageable pageable) {
        return bookRepository.findAvailableBooks(pageable)
                .map(this::mapToResponse);
    }

    // Helper methods
    private BookResponse mapToResponse(Book book) {
        return new BookResponse(
                book.getId(),
                book.getTitle(),
                book.getAuthor(),
                book.getDescription(),
                book.getPrice(),
                book.getStockQuantity(),
                book.getIsbn(),
                book.getGenre(),
                book.getPublicationYear(),
                book.getPages(),
                book.getPublisher(),
                book.getImageUrl(),
                book.getAverageRating(),
                book.getTotalReviews(),
                book.getIsActive()
        );
    }

    public Book getBookEntityById(UUID bookId) {
        return bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found"));
    }
}

