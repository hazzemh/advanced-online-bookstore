package com.example.bookstore.book.service;

import com.example.bookstore.book.dto.BookResponse;
import com.example.bookstore.book.dto.CreateBookRequest;
import com.example.bookstore.book.dto.UpdateBookRequest;
import com.example.bookstore.book.entity.Book;
import com.example.bookstore.book.repository.BookRepository;
import com.example.bookstore.config.CacheConfig;
import com.example.bookstore.common.service.FileService;
import org.springframework.cache.annotation.Caching;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class BookService {

    private final BookRepository bookRepository;
    private final FileService fileService;

    public BookService(BookRepository bookRepository, FileService fileService) {
        this.bookRepository = bookRepository;
        this.fileService = fileService;
    }

    // Admin Operations
    @Caching(evict = {
            @CacheEvict(value = CacheConfig.BOOK_LIST_CACHE, allEntries = true),
            @CacheEvict(value = CacheConfig.BOOK_BY_GENRE_CACHE, allEntries = true),
            @CacheEvict(value = CacheConfig.BOOK_SEARCH_TITLE_CACHE, allEntries = true),
            @CacheEvict(value = CacheConfig.BOOK_SEARCH_AUTHOR_CACHE, allEntries = true),
            @CacheEvict(value = CacheConfig.BOOK_TOP_RATED_CACHE, allEntries = true),
            @CacheEvict(value = CacheConfig.BOOK_AVAILABLE_CACHE, allEntries = true),
            @CacheEvict(value = CacheConfig.GENRES_CACHE, allEntries = true)
    })
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

    @Caching(evict = {
            @CacheEvict(value = CacheConfig.BOOK_BY_ID_CACHE, key = "#bookId"),
            @CacheEvict(value = CacheConfig.BOOK_LIST_CACHE, allEntries = true),
            @CacheEvict(value = CacheConfig.BOOK_BY_GENRE_CACHE, allEntries = true),
            @CacheEvict(value = CacheConfig.BOOK_SEARCH_TITLE_CACHE, allEntries = true),
            @CacheEvict(value = CacheConfig.BOOK_SEARCH_AUTHOR_CACHE, allEntries = true),
            @CacheEvict(value = CacheConfig.BOOK_TOP_RATED_CACHE, allEntries = true),
            @CacheEvict(value = CacheConfig.BOOK_AVAILABLE_CACHE, allEntries = true),
            @CacheEvict(value = CacheConfig.GENRES_CACHE, allEntries = true)
    })
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

    @Caching(evict = {
            @CacheEvict(value = CacheConfig.BOOK_BY_ID_CACHE, key = "#bookId"),
            @CacheEvict(value = CacheConfig.BOOK_LIST_CACHE, allEntries = true),
            @CacheEvict(value = CacheConfig.BOOK_BY_GENRE_CACHE, allEntries = true),
            @CacheEvict(value = CacheConfig.BOOK_SEARCH_TITLE_CACHE, allEntries = true),
            @CacheEvict(value = CacheConfig.BOOK_SEARCH_AUTHOR_CACHE, allEntries = true),
            @CacheEvict(value = CacheConfig.BOOK_TOP_RATED_CACHE, allEntries = true),
            @CacheEvict(value = CacheConfig.BOOK_AVAILABLE_CACHE, allEntries = true),
            @CacheEvict(value = CacheConfig.GENRES_CACHE, allEntries = true)
    })
    public void deleteBook(UUID bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found"));
        book.setIsActive(false);
        bookRepository.save(book);
    }

    // Public Operations
    @Cacheable(value = CacheConfig.BOOK_BY_ID_CACHE, key = "#bookId")
    public BookResponse getBookById(UUID bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found"));
        return mapToResponse(book);
    }

    @Cacheable(
            value = CacheConfig.BOOK_LIST_CACHE,
            key = "'p=' + #pageable.pageNumber + '|s=' + #pageable.pageSize + '|sort=' + #pageable.sort.toString()"
    )
    public Page<BookResponse> getAllBooks(Pageable pageable) {
        return bookRepository.findByIsActive(true, pageable)
                .map(this::mapToResponse);
    }

    @Cacheable(
            value = CacheConfig.BOOK_SEARCH_TITLE_CACHE,
            key = "'kw=' + #keyword + '|p=' + #pageable.pageNumber + '|s=' + #pageable.pageSize + '|sort=' + #pageable.sort.toString()"
    )
    public Page<BookResponse> searchByTitle(String keyword, Pageable pageable) {
        return bookRepository.searchByTitle(keyword, pageable)
                .map(this::mapToResponse);
    }

    @Cacheable(
            value = CacheConfig.BOOK_SEARCH_AUTHOR_CACHE,
            key = "'kw=' + #keyword + '|p=' + #pageable.pageNumber + '|s=' + #pageable.pageSize + '|sort=' + #pageable.sort.toString()"
    )
    public Page<BookResponse> searchByAuthor(String keyword, Pageable pageable) {
        return bookRepository.searchByAuthor(keyword, pageable)
                .map(this::mapToResponse);
    }

    @Cacheable(
            value = CacheConfig.BOOK_BY_GENRE_CACHE,
            key = "'g=' + #genre + '|p=' + #pageable.pageNumber + '|s=' + #pageable.pageSize + '|sort=' + #pageable.sort.toString()"
    )
    public Page<BookResponse> findByGenre(String genre, Pageable pageable) {
        return bookRepository.findByGenre(genre, pageable)
                .map(this::mapToResponse);
    }

    public Page<BookResponse> findByPriceRange(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {
        return bookRepository.findByPriceRange(minPrice, maxPrice, pageable)
                .map(this::mapToResponse);
    }

    @Cacheable(
            value = CacheConfig.BOOK_TOP_RATED_CACHE,
            key = "'p=' + #pageable.pageNumber + '|s=' + #pageable.pageSize + '|sort=' + #pageable.sort.toString()"
    )
    public Page<BookResponse> findTopRatedBooks(Pageable pageable) {
        return bookRepository.findTopRatedBooks(pageable)
                .map(this::mapToResponse);
    }

    @Cacheable(
            value = CacheConfig.BOOK_AVAILABLE_CACHE,
            key = "'p=' + #pageable.pageNumber + '|s=' + #pageable.pageSize + '|sort=' + #pageable.sort.toString()"
    )
    public Page<BookResponse> findAvailableBooks(Pageable pageable) {
        return bookRepository.findAvailableBooks(pageable)
                .map(this::mapToResponse);
    }

    @Cacheable(CacheConfig.GENRES_CACHE)
    public List<String> getGenres() {
        return bookRepository.findDistinctActiveGenres();
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

    /**
     * Cross-module helper: load a book entity and ensure it's active/available.
     * Prefer this over other modules using BookRepository directly.
     */
    public Book requireActiveBookEntity(UUID bookId) {
        Book book = getBookEntityById(bookId);
        if (!Boolean.TRUE.equals(book.getIsActive())) {
            throw new RuntimeException("Book is not available");
        }
        return book;
    }

    /**
     * Cross-module helper: bulk load books by id. Missing ids are omitted.
     */
    public Map<UUID, Book> loadBooksById(Collection<UUID> bookIds) {
        if (bookIds == null || bookIds.isEmpty()) {
            return Map.of();
        }
        Map<UUID, Book> byId = new HashMap<>();
        for (Book b : bookRepository.findAllById(bookIds)) {
            if (b != null && b.getId() != null) {
                byId.put(b.getId(), b);
            }
        }
        return byId;
    }

    /**
     * Cross-module helper: persist a Book entity (e.g., stock updates from OrderService).
     */
    public Book saveBookEntity(Book book) {
        return bookRepository.save(book);
    }

    /**
     * Cross-module helper for recommendation fallback logic.
     */
    public List<Book> getAllBookEntities() {
        return bookRepository.findAll();
    }

    @Caching(evict = {
            @CacheEvict(value = CacheConfig.BOOK_BY_ID_CACHE, key = "#bookId"),
            @CacheEvict(value = CacheConfig.BOOK_LIST_CACHE, allEntries = true),
            @CacheEvict(value = CacheConfig.BOOK_BY_GENRE_CACHE, allEntries = true),
            @CacheEvict(value = CacheConfig.BOOK_SEARCH_TITLE_CACHE, allEntries = true),
            @CacheEvict(value = CacheConfig.BOOK_SEARCH_AUTHOR_CACHE, allEntries = true),
            @CacheEvict(value = CacheConfig.BOOK_TOP_RATED_CACHE, allEntries = true),
            @CacheEvict(value = CacheConfig.BOOK_AVAILABLE_CACHE, allEntries = true)
    })
    @Transactional
    public Book decrementStock(UUID bookId, int quantity) {
        if (bookId == null) {
            throw new IllegalArgumentException("bookId is required");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be >= 1");
        }
        Book book = requireActiveBookEntity(bookId);
        Integer current = book.getStockQuantity() == null ? 0 : book.getStockQuantity();
        if (current <= 0) {
            throw new IllegalArgumentException("Book is out of stock");
        }
        if (quantity > current) {
            throw new IllegalArgumentException("Requested quantity exceeds available stock");
        }
        book.setStockQuantity(current - quantity);
        return bookRepository.save(book);
    }

    @Caching(evict = {
            @CacheEvict(value = CacheConfig.BOOK_BY_ID_CACHE, key = "#bookId"),
            @CacheEvict(value = CacheConfig.BOOK_LIST_CACHE, allEntries = true),
            @CacheEvict(value = CacheConfig.BOOK_BY_GENRE_CACHE, allEntries = true),
            @CacheEvict(value = CacheConfig.BOOK_SEARCH_TITLE_CACHE, allEntries = true),
            @CacheEvict(value = CacheConfig.BOOK_SEARCH_AUTHOR_CACHE, allEntries = true),
            @CacheEvict(value = CacheConfig.BOOK_TOP_RATED_CACHE, allEntries = true),
            @CacheEvict(value = CacheConfig.BOOK_AVAILABLE_CACHE, allEntries = true)
    })
    @Transactional
    public Book incrementStock(UUID bookId, int quantity) {
        if (bookId == null) {
            throw new IllegalArgumentException("bookId is required");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be >= 1");
        }
        Book book = getBookEntityById(bookId);
        Integer current = book.getStockQuantity() == null ? 0 : book.getStockQuantity();
        book.setStockQuantity(current + quantity);
        return bookRepository.save(book);
    }

    @Caching(evict = {
            @CacheEvict(value = CacheConfig.BOOK_BY_ID_CACHE, key = "#bookId"),
            @CacheEvict(value = CacheConfig.BOOK_LIST_CACHE, allEntries = true),
            @CacheEvict(value = CacheConfig.BOOK_BY_GENRE_CACHE, allEntries = true),
            @CacheEvict(value = CacheConfig.BOOK_SEARCH_TITLE_CACHE, allEntries = true),
            @CacheEvict(value = CacheConfig.BOOK_SEARCH_AUTHOR_CACHE, allEntries = true),
            @CacheEvict(value = CacheConfig.BOOK_TOP_RATED_CACHE, allEntries = true),
            @CacheEvict(value = CacheConfig.BOOK_AVAILABLE_CACHE, allEntries = true)
    })
    @Transactional
    public Book updateRatingStats(UUID bookId, double averageRating, int totalReviews) {
        if (bookId == null) {
            throw new IllegalArgumentException("bookId is required");
        }
        if (totalReviews < 0) {
            throw new IllegalArgumentException("totalReviews must be >= 0");
        }
        Book book = getBookEntityById(bookId);
        book.setAverageRating(averageRating);
        book.setTotalReviews(totalReviews);
        return bookRepository.save(book);
    }

    // Image Upload Operations
    @Caching(evict = {
            @CacheEvict(value = CacheConfig.BOOK_BY_ID_CACHE, key = "#bookId"),
            @CacheEvict(value = CacheConfig.BOOK_LIST_CACHE, allEntries = true),
            @CacheEvict(value = CacheConfig.BOOK_BY_GENRE_CACHE, allEntries = true),
            @CacheEvict(value = CacheConfig.BOOK_SEARCH_TITLE_CACHE, allEntries = true),
            @CacheEvict(value = CacheConfig.BOOK_SEARCH_AUTHOR_CACHE, allEntries = true),
            @CacheEvict(value = CacheConfig.BOOK_TOP_RATED_CACHE, allEntries = true),
            @CacheEvict(value = CacheConfig.BOOK_AVAILABLE_CACHE, allEntries = true)
    })
    public BookResponse uploadBookImage(UUID bookId, MultipartFile imageFile) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found"));

        // Delete existing image if present
        if (book.getImagePath() != null) {
            fileService.deleteImage(book.getImagePath());
        }

        // Upload new image
        String imagePath = fileService.uploadImage(imageFile);
        String imageUrl = "/uploads/" + imagePath;

        book.setImagePath(imagePath);
        book.setImageUrl(imageUrl);

        Book updatedBook = bookRepository.save(book);
        return mapToResponse(updatedBook);
    }

    @Caching(evict = {
            @CacheEvict(value = CacheConfig.BOOK_BY_ID_CACHE, key = "#bookId"),
            @CacheEvict(value = CacheConfig.BOOK_LIST_CACHE, allEntries = true),
            @CacheEvict(value = CacheConfig.BOOK_BY_GENRE_CACHE, allEntries = true),
            @CacheEvict(value = CacheConfig.BOOK_SEARCH_TITLE_CACHE, allEntries = true),
            @CacheEvict(value = CacheConfig.BOOK_SEARCH_AUTHOR_CACHE, allEntries = true),
            @CacheEvict(value = CacheConfig.BOOK_TOP_RATED_CACHE, allEntries = true),
            @CacheEvict(value = CacheConfig.BOOK_AVAILABLE_CACHE, allEntries = true)
    })
    public void deleteBookImage(UUID bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found"));

        if (book.getImagePath() != null) {
            fileService.deleteImage(book.getImagePath());
            book.setImagePath(null);
            book.setImageUrl(null);
            bookRepository.save(book);
        }
    }
}
