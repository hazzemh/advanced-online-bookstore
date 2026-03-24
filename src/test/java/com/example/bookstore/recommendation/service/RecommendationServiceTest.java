package com.example.bookstore.recommendation.service;

import com.example.bookstore.book.entity.Book;
import com.example.bookstore.book.repository.BookRepository;
import com.example.bookstore.cart.entity.Cart;
import com.example.bookstore.cart.entity.CartItem;
import com.example.bookstore.cart.repository.CartItemRepository;
import com.example.bookstore.cart.repository.CartRepository;
import com.example.bookstore.order.entity.Order;
import com.example.bookstore.order.entity.OrderItem;
import com.example.bookstore.order.entity.OrderStatus;
import com.example.bookstore.order.repository.OrderItemRepository;
import com.example.bookstore.order.repository.OrderRepository;
import com.example.bookstore.review.entity.Review;
import com.example.bookstore.review.repository.ReviewRepository;
import com.example.bookstore.user.entity.Role;
import com.example.bookstore.user.entity.User;
import com.example.bookstore.user.repository.UserRepository;
import com.example.bookstore.wishlist.entity.WishlistItem;
import com.example.bookstore.wishlist.repository.WishlistRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class RecommendationServiceTest {

    @Autowired
    private RecommendationService recommendationService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private WishlistRepository wishlistRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @BeforeEach
    void setUp() {
        reviewRepository.deleteAll();
        wishlistRepository.deleteAll();
        cartItemRepository.deleteAll();
        cartRepository.deleteAll();
        orderItemRepository.deleteAll();
        orderRepository.deleteAll();
        bookRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void contentBased_shouldRecommendSameGenre() {
        User u1 = userRepository.save(User.builder()
                .email("u1@example.com")
                .password("x")
                .role(Role.USER)
                .enabled(true)
                .build());

        Book b1 = bookRepository.save(Book.builder()
                .title("Fic 1")
                .author("AuthorA")
                .description("d")
                .price(new BigDecimal("10.00"))
                .stockQuantity(10)
                .pages(100)
                .publisher("P")
                .genre("Fiction")
                .build());

        Book b2 = bookRepository.save(Book.builder()
                .title("Fic 2")
                .author("AuthorB")
                .description("d")
                .price(new BigDecimal("12.00"))
                .stockQuantity(10)
                .pages(100)
                .publisher("P")
                .genre("Fiction")
                .build());

        Book b3 = bookRepository.save(Book.builder()
                .title("Sci 1")
                .author("AuthorC")
                .description("d")
                .price(new BigDecimal("15.00"))
                .stockQuantity(10)
                .pages(100)
                .publisher("P")
                .genre("Science")
                .build());

        wishlistRepository.save(WishlistItem.builder()
                .user(u1)
                .book(b1)
                .build());

        List<com.example.bookstore.book.dto.BookResponse> recs =
                recommendationService.recommend(u1.getEmail(), RecommendationStrategy.CONTENT, 10);

        assertTrue(containsBookId(recs, b2.getId()));
        assertFalse(containsBookId(recs, b1.getId()));
        assertFalse(containsBookId(recs, b3.getId()));
    }

    @Test
    void orders_shouldActAsPositiveSignalForContentBased() {
        User u1 = userRepository.save(User.builder()
                .email("u1@example.com")
                .password("x")
                .role(Role.USER)
                .enabled(true)
                .build());

        Book purchased = bookRepository.save(Book.builder()
                .title("Fic 1")
                .author("AuthorA")
                .description("d")
                .price(new BigDecimal("10.00"))
                .stockQuantity(10)
                .pages(100)
                .publisher("P")
                .genre("Fiction")
                .build());

        Book candidateSameGenre = bookRepository.save(Book.builder()
                .title("Fic 2")
                .author("AuthorB")
                .description("d")
                .price(new BigDecimal("12.00"))
                .stockQuantity(10)
                .pages(100)
                .publisher("P")
                .genre("Fiction")
                .build());

        Book candidateOtherGenre = bookRepository.save(Book.builder()
                .title("Sci 1")
                .author("AuthorC")
                .description("d")
                .price(new BigDecimal("15.00"))
                .stockQuantity(10)
                .pages(100)
                .publisher("P")
                .genre("Science")
                .build());

        Order order = Order.builder()
                .user(u1)
                .status(OrderStatus.PROCESSING)
                .subtotal(BigDecimal.ZERO)
                .build();
        order.getItems().add(OrderItem.builder()
                .order(order)
                .book(purchased)
                .quantity(1)
                .unitPrice(purchased.getPrice())
                .lineTotal(purchased.getPrice())
                .build());
        orderRepository.save(order);

        List<com.example.bookstore.book.dto.BookResponse> recs =
                recommendationService.recommend(u1.getEmail(), RecommendationStrategy.CONTENT, 10);

        assertTrue(containsBookId(recs, candidateSameGenre.getId()));
        assertFalse(containsBookId(recs, purchased.getId()));
        assertFalse(containsBookId(recs, candidateOtherGenre.getId()));
    }

    @Test
    void collaborative_shouldRecommendBooksFromSimilarUsers() {
        User u1 = userRepository.save(User.builder()
                .email("u1@example.com")
                .password("x")
                .role(Role.USER)
                .enabled(true)
                .build());
        User u2 = userRepository.save(User.builder()
                .email("u2@example.com")
                .password("x")
                .role(Role.USER)
                .enabled(true)
                .build());

        Book shared = bookRepository.save(Book.builder()
                .title("Shared")
                .author("A")
                .description("d")
                .price(new BigDecimal("10.00"))
                .stockQuantity(10)
                .pages(100)
                .publisher("P")
                .genre("Fiction")
                .build());

        Book other = bookRepository.save(Book.builder()
                .title("Other")
                .author("B")
                .description("d")
                .price(new BigDecimal("11.00"))
                .stockQuantity(10)
                .pages(100)
                .publisher("P")
                .genre("Science")
                .build());

        wishlistRepository.save(WishlistItem.builder()
                .user(u1)
                .book(shared)
                .build());

        wishlistRepository.save(WishlistItem.builder()
                .user(u2)
                .book(shared)
                .build());
        wishlistRepository.save(WishlistItem.builder()
                .user(u2)
                .book(other)
                .build());

        List<com.example.bookstore.book.dto.BookResponse> recs =
                recommendationService.recommend(u1.getEmail(), RecommendationStrategy.COLLABORATIVE, 10);

        assertTrue(containsBookId(recs, other.getId()));
        assertFalse(containsBookId(recs, shared.getId()));
    }

    @Test
    void collaborative_shouldUseOrdersAsSignal() {
        User u1 = userRepository.save(User.builder()
                .email("u1@example.com")
                .password("x")
                .role(Role.USER)
                .enabled(true)
                .build());
        User u2 = userRepository.save(User.builder()
                .email("u2@example.com")
                .password("x")
                .role(Role.USER)
                .enabled(true)
                .build());

        Book shared = bookRepository.save(Book.builder()
                .title("Shared")
                .author("A")
                .description("d")
                .price(new BigDecimal("10.00"))
                .stockQuantity(10)
                .pages(100)
                .publisher("P")
                .genre("Fiction")
                .build());

        Book other = bookRepository.save(Book.builder()
                .title("Other")
                .author("B")
                .description("d")
                .price(new BigDecimal("11.00"))
                .stockQuantity(10)
                .pages(100)
                .publisher("P")
                .genre("Science")
                .build());

        Order o1 = Order.builder().user(u1).status(OrderStatus.PROCESSING).subtotal(BigDecimal.ZERO).build();
        o1.getItems().add(OrderItem.builder()
                .order(o1).book(shared).quantity(1).unitPrice(shared.getPrice()).lineTotal(shared.getPrice()).build());
        orderRepository.save(o1);

        Order o2 = Order.builder().user(u2).status(OrderStatus.PROCESSING).subtotal(BigDecimal.ZERO).build();
        o2.getItems().add(OrderItem.builder()
                .order(o2).book(shared).quantity(1).unitPrice(shared.getPrice()).lineTotal(shared.getPrice()).build());
        o2.getItems().add(OrderItem.builder()
                .order(o2).book(other).quantity(1).unitPrice(other.getPrice()).lineTotal(other.getPrice()).build());
        orderRepository.save(o2);

        List<com.example.bookstore.book.dto.BookResponse> recs =
                recommendationService.recommend(u1.getEmail(), RecommendationStrategy.COLLABORATIVE, 10);

        assertTrue(containsBookId(recs, other.getId()));
        assertFalse(containsBookId(recs, shared.getId()));
    }

    @Test
    void shouldExcludeBooksUserAlreadyReviewedEvenIfOtherwiseRecommended() {
        User u1 = userRepository.save(User.builder()
                .email("u1@example.com")
                .password("x")
                .role(Role.USER)
                .enabled(true)
                .build());
        User u2 = userRepository.save(User.builder()
                .email("u2@example.com")
                .password("x")
                .role(Role.USER)
                .enabled(true)
                .build());

        Book shared = bookRepository.save(Book.builder()
                .title("Shared")
                .author("A")
                .description("d")
                .price(new BigDecimal("10.00"))
                .stockQuantity(10)
                .pages(100)
                .publisher("P")
                .genre("Fiction")
                .build());

        Book other = bookRepository.save(Book.builder()
                .title("Other")
                .author("B")
                .description("d")
                .price(new BigDecimal("11.00"))
                .stockQuantity(10)
                .pages(100)
                .publisher("P")
                .genre("Science")
                .build());

        wishlistRepository.save(WishlistItem.builder()
                .user(u1)
                .book(shared)
                .build());
        wishlistRepository.save(WishlistItem.builder()
                .user(u2)
                .book(shared)
                .build());
        wishlistRepository.save(WishlistItem.builder()
                .user(u2)
                .book(other)
                .build());

        reviewRepository.save(Review.builder()
                .user(u1)
                .book(other)
                .rating(3)
                .comment("ok")
                .build());

        List<com.example.bookstore.book.dto.BookResponse> recs =
                recommendationService.recommend(u1.getEmail(), RecommendationStrategy.COLLABORATIVE, 10);
        assertFalse(containsBookId(recs, other.getId()));
    }

    @Test
    void preferenceProfile_shouldIncludeWishlistAndCartSignals() {
        User u1 = userRepository.save(User.builder()
                .email("u1@example.com")
                .password("x")
                .role(Role.USER)
                .enabled(true)
                .build());

        Book b1 = bookRepository.save(Book.builder()
                .title("Fic 1")
                .author("AuthorA")
                .description("d")
                .price(new BigDecimal("10.00"))
                .stockQuantity(10)
                .pages(100)
                .publisher("P")
                .genre("Fiction")
                .build());

        Book b2 = bookRepository.save(Book.builder()
                .title("Sci 1")
                .author("AuthorA")
                .description("d")
                .price(new BigDecimal("12.00"))
                .stockQuantity(10)
                .pages(100)
                .publisher("P")
                .genre("Science")
                .build());

        wishlistRepository.save(WishlistItem.builder()
                .user(u1)
                .book(b1)
                .build());

        Cart cart = cartRepository.save(Cart.builder().user(u1).build());
        cartItemRepository.save(CartItem.builder()
                .cart(cart)
                .book(b2)
                .quantity(1)
                .unitPrice(b2.getPrice())
                .build());

        var profile = recommendationService.getUserPreferences(u1.getEmail(), 5, 5);
        assertTrue(profile.positiveSignals() >= 2);
        assertTrue(profile.topAuthors().stream().anyMatch(s -> s.key().equalsIgnoreCase("authora")));
    }

    private boolean containsBookId(List<com.example.bookstore.book.dto.BookResponse> recs, UUID id) {
        return recs.stream().anyMatch(r -> id.equals(r.id()));
    }
}
