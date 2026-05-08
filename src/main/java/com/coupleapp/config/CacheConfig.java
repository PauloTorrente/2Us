package com.coupleapp.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

// Imports comentados — Redis desabilitado em DEV
// import org.springframework.context.annotation.Primary;
// import org.springframework.data.redis.cache.RedisCacheConfiguration;
// import org.springframework.data.redis.cache.RedisCacheManager;
// import org.springframework.data.redis.connection.RedisConnectionFactory;
// import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
// import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

// Configures caching with Caffeine for local development (in-memory).
// Redis configuration is commented out and available in RedisConfig.java for production use.
// To enable Redis in production, uncomment RedisConfig.java and re-add the spring-boot-starter-data-redis dependency in pom.xml
@Configuration
@EnableCaching
public class CacheConfig {

    @Value("${app.cache.ttl-hours:1}")
    private int cacheTtlHours;

    // Redis cache manager — DESABILITADO (comentado em RedisConfig.java)
    // Descomente apenas em produção quando Redis estiver disponível
    /*
    @Bean
    @Primary
    @Profile("prod")
    public CacheManager redisCacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofHours(cacheTtlHours))
            .serializeValuesWith(
                RedisSerializationContext.SerializationPair.fromSerializer(
                    new GenericJackson2JsonRedisSerializer()
                )
            );

        return RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(config)
            .build();
    }
    */

    // Caffeine cache manager — used in local development (no Redis required)
    @Bean
    @Profile("dev")
    public CacheManager caffeineCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(
            "googlePlacesCache",
            "tripAdvisorCache"
        );
        
        cacheManager.setCaffeine(Caffeine.newBuilder()
            .expireAfterWrite(cacheTtlHours, TimeUnit.HOURS)
            .maximumSize(500)
            .recordStats());
        
        return cacheManager;
    }
}
