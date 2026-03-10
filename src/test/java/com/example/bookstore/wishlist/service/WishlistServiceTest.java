package com.example.bookstore.wishlist.service;

import com.example.bookstore.book.entity.Book;
import com.example.bookstore.book.repository.BookRepository;
import com.example.bookstore.user.entity.Role;
import com.example.bookstore.user.entity.User;
import com.example.bookstore.user.repository.UserRepository;
import com.example.bookstore.wishlist.dto.WishlistResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class WishlistServiceTest {

    @Autowired
    private WishlistService wishlistService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookRepository bookRepository;

    private User user;
    private User secondUser;
    private Book book;

    @BeforeEach
    void setUp() {
        String suffix = UUID.randomUUID().toString();

        user = userRepository.save(User.builder()
                .email("wishlist.user1+" + suffix + "@test.com")
                .password("password")
                .firstName("Wishlist")
                .lastName("UserOne")
                .role(Role.USER)
                .build());

        secondUser = userRepository.save(User.builder()
                .email("wishlist.user2+" + suffix + "@test.com")
                .password("password")
                .firstName("Wishlist")
                .lastName("UserTwo")
                .role(Role.USER)
                .build());

        book = bookRepository.save(Book.builder()
                .title("Clean Code")
                .author("Robert C. Martin")
                .description("A handbook of agile software craftsmanship")
                .price(new BigDecimal("29.99"))
                .stockQuantity(10)
                .isbn("9780132350884-" + suffix.substring(0, 8))
                .genre("Programming")
                .publicationYear(2008)
                .pages(464)
                .publisher("Prentice Hall")
                .build());
    }

    @Test
    void testAddBookToWishlist_Success() {
        WishlistResponse response = wishlistService.addBookToWishlist(book.getId(), user.getEmail());

        assertNotNull(response.wishlistItemId());
        assertEquals(book.getId(), response.bookId());
        assertEquals("Clean Code", response.title());
    }

    @Test
    void testAddBookToWishlist_DuplicateBook() {
        wishlistService.addBookToWishlist(book.getId(), user.getEmail());

        assertThrows(RuntimeException.class, () ->
                wishlistService.addBookToWishlist(book.getId(), user.getEmail()));
    }

    @Test
    void testGetUserWishlist_UserSpecific() {
        wishlistService.addBookToWishlist(book.getId(), user.getEmail());
        wishlistService.addBookToWishlist(book.getId(), secondUser.getEmail());

        Pageable pageable = PageRequest.of(0, 10);
        Page<WishlistResponse> firstUserWishlist = wishlistService.getUserWishlist(user.getEmail(), pageable);
        Page<WishlistResponse> secondUserWishlist = wishlistService.getUserWishlist(secondUser.getEmail(), pageable);

        assertEquals(1, firstUserWishlist.getTotalElements());
        assertEquals(1, secondUserWishlist.getTotalElements());
        assertEquals(book.getId(), firstUserWishlist.getContent().get(0).bookId());
    }

    @Test
    void testRemoveBookFromWishlist_Success() {
        wishlistService.addBookToWishlist(book.getId(), user.getEmail());

        wishlistService.removeBookFromWishlist(book.getId(), user.getEmail());

        assertFalse(wishlistService.isBookInWishlist(book.getId(), user.getEmail()));
    }
}
