package com.fooddelivery.menuservice.service;

import com.fooddelivery.menuservice.dto.RestaurantDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class CacheService {

    private static final Logger logger = LoggerFactory.getLogger(CacheService.class);
    private static final String CACHE_KEY_PREFIX = "restaurant:menu:";

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private com.github.benmanes.caffeine.cache.Cache<String, Object> caffeineCache;

    @org.springframework.beans.factory.annotation.Value("${cache.redis.ttl:3600}")
    private long redisTtl;

    // L1 Cache Operations (Caffeine)
    public RestaurantDTO getFromL1Cache(UUID restaurantId) {
        String key = CACHE_KEY_PREFIX + restaurantId.toString();
        RestaurantDTO restaurant = (RestaurantDTO) caffeineCache.getIfPresent(key);

        if (restaurant != null) {
            logger.info("Cache HIT in L1 (Caffeine) for restaurant: {}", restaurantId);
            return restaurant;
        }

        logger.debug("Cache MISS in L1 (Caffeine) for restaurant: {}", restaurantId);
        return null;
    }

    // L2 Cache Operations (Redis)
    public RestaurantDTO getFromL2Cache(UUID restaurantId) {
        try {
            String key = CACHE_KEY_PREFIX + restaurantId.toString();
            Object cachedValue = redisTemplate.opsForValue().get(key);

            if (cachedValue != null) {
                logger.info("Cache HIT in L2 (Redis) for restaurant: {}", restaurantId);
                RestaurantDTO restaurant = (RestaurantDTO) cachedValue;

                // Update L1 cache when retrieving from L2
                updateL1Cache(restaurantId, restaurant);
                return restaurant;
            }

            logger.debug("Cache MISS in L2 (Redis) for restaurant: {}", restaurantId);
            return null;
        } catch (Exception e) {
            logger.error("Error retrieving from L2 cache for restaurant: {}: {}", restaurantId, e.getMessage());
            return null;
        }
    }

    // Update L1 Cache
    public void updateL1Cache(UUID restaurantId, RestaurantDTO restaurant) {
        try {
            String key = CACHE_KEY_PREFIX + restaurantId.toString();
            caffeineCache.put(key, restaurant);
            logger.debug("L1 Cache updated for restaurant: {}", restaurantId);
        } catch (Exception e) {
            logger.error("Error updating L1 cache for restaurant: {}: {}", restaurantId, e.getMessage());
        }
    }

    // Update L2 Cache
    public void updateL2Cache(UUID restaurantId, RestaurantDTO restaurant) {
        try {
            String key = CACHE_KEY_PREFIX + restaurantId.toString();
            redisTemplate.opsForValue().set(key, restaurant, redisTtl, TimeUnit.SECONDS);
            logger.debug("L2 Cache updated for restaurant: {} with TTL: {} seconds", restaurantId, redisTtl);
        } catch (Exception e) {
            logger.error("Error updating L2 cache for restaurant: {}: {}", restaurantId, e.getMessage());
        }
    }

    // Update both caches
    public void updateBothCaches(UUID restaurantId, RestaurantDTO restaurant) {
        logger.debug("Updating both L1 and L2 caches for restaurant: {}", restaurantId);
        updateL1Cache(restaurantId, restaurant);
        updateL2Cache(restaurantId, restaurant);
    }

    // Clear both caches
    public void clearBothCaches(UUID restaurantId) {
        try {
            String key = CACHE_KEY_PREFIX + restaurantId.toString();
            caffeineCache.invalidate(key);
            redisTemplate.delete(key);
            logger.info("Both caches cleared for restaurant: {}", restaurantId);
        } catch (Exception e) {
            logger.error("Error clearing caches for restaurant: {}: {}", restaurantId, e.getMessage());
        }
    }
}