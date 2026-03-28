package com.example.bookstore.recommendation.service;

import com.example.bookstore.config.CacheConfig;
import com.example.bookstore.order.event.OrderCreatedEvent;
import com.example.bookstore.order.event.OrderStatusChangedEvent;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
public class RecommendationCacheEvictor {

    private final CacheManager cacheManager;

    public RecommendationCacheEvictor(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @EventListener
    public void onOrderCreated(OrderCreatedEvent event) {
        evictRecommendationCaches();
    }

    @EventListener
    public void onOrderStatusChanged(OrderStatusChangedEvent event) {
        evictRecommendationCaches();
    }

    private void evictRecommendationCaches() {
        Cache recs = cacheManager.getCache(CacheConfig.RECOMMENDATIONS_CACHE);
        if (recs != null) {
            recs.clear();
        }
        Cache profile = cacheManager.getCache(CacheConfig.RECOMMENDATION_PROFILE_CACHE);
        if (profile != null) {
            profile.clear();
        }
    }
}

