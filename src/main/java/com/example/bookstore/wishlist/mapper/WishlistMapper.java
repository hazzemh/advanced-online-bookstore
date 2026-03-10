package com.example.bookstore.wishlist.mapper;

import com.example.bookstore.book.entity.Book;
import com.example.bookstore.wishlist.dto.WishlistResponse;
import com.example.bookstore.wishlist.entity.WishlistItem;
import org.springframework.stereotype.Component;

@Component
public class WishlistMapper {

    public WishlistResponse mapToResponse(WishlistItem wishlistItem) {
        Book book = wishlistItem.getBook();
        return new WishlistResponse(
                wishlistItem.getId(),
                book.getId(),
                book.getTitle(),
                book.getAuthor(),
                book.getPrice(),
                book.getImageUrl(),
                book.getAverageRating(),
                book.getTotalReviews(),
                wishlistItem.getAddedAt()
        );
    }
}
