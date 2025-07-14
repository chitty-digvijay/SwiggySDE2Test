package com.fooddelivery.menuservice.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import redis.embedded.RedisServer;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.IOException;

@Configuration
public class EmbeddedRedisConfig {

    private static final Logger logger = LoggerFactory.getLogger(EmbeddedRedisConfig.class);
    private RedisServer redisServer;

    @PostConstruct
    public void startRedis() throws IOException {
        try {
            redisServer = RedisServer.builder()
                    .port(6379)
                    .build();
            redisServer.start();
            logger.info("Embedded Redis server started successfully on port: 6379");
        } catch (Exception e) {
            logger.warn("Could not start embedded Redis server: {}. Redis caching may not work.", e.getMessage());
        }
    }

    @PreDestroy
    public void stopRedis() {
        if (redisServer != null && redisServer.isActive()) {
            redisServer.stop();
            logger.info("Embedded Redis server stopped");
        }
    }
}