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
}
