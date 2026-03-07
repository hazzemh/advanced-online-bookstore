package com.example.bookstore.book.repository;

import com.example.bookstore.book.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

public interface BookRepository extends JpaRepository<Book, UUID> {

    Optional<Book> findByIsbn(String isbn);

    Page<Book> findByIsActive(Boolean isActive, Pageable pageable);

    @Query("SELECT b FROM Book b WHERE b.isActive = true AND LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Book> searchByTitle(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT b FROM Book b WHERE b.isActive = true AND LOWER(b.author) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Book> searchByAuthor(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT b FROM Book b WHERE b.isActive = true AND LOWER(b.genre) LIKE LOWER(CONCAT('%', :genre, '%'))")
    Page<Book> findByGenre(@Param("genre") String genre, Pageable pageable);

    @Query("SELECT b FROM Book b WHERE b.isActive = true AND b.price BETWEEN :minPrice AND :maxPrice")
    Page<Book> findByPriceRange(@Param("minPrice") BigDecimal minPrice, @Param("maxPrice") BigDecimal maxPrice, Pageable pageable);

    @Query("SELECT b FROM Book b WHERE b.isActive = true ORDER BY b.averageRating DESC")
    Page<Book> findTopRatedBooks(Pageable pageable);

    @Query("SELECT b FROM Book b WHERE b.isActive = true AND b.stockQuantity > 0")
    Page<Book> findAvailableBooks(Pageable pageable);
}

