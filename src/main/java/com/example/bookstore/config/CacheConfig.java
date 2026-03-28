package com.example.bookstore.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * Keep cache names centralized to avoid typos across @Cacheable/@CacheEvict.
     */
    public static final String BOOK_BY_ID_CACHE = "bookById";

    public static final String BOOK_LIST_CACHE = "bookList";
    public static final String BOOK_BY_GENRE_CACHE = "bookByGenre";
    public static final String BOOK_SEARCH_TITLE_CACHE = "bookSearchTitle";
    public static final String BOOK_SEARCH_AUTHOR_CACHE = "bookSearchAuthor";
    public static final String BOOK_TOP_RATED_CACHE = "bookTopRated";
    public static final String BOOK_AVAILABLE_CACHE = "bookAvailable";
    public static final String GENRES_CACHE = "genres";

    public static final String RECOMMENDATIONS_CACHE = "recommendations";
    public static final String RECOMMENDATION_PROFILE_CACHE = "recommendationProfile";
}
