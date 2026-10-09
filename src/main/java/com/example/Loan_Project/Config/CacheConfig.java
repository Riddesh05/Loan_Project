package com.example.Loan_Project.Config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;

import org.springframework.data.redis.connection.RedisConnectionFactory;

import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public RedisCacheConfiguration redisCacheConfiguration() {

        return RedisCacheConfiguration
                .defaultCacheConfig()

                // Cache data for 10 minutes
                .entryTtl(
                        Duration.ofMinutes(10)
                )

                // Do not cache null values
                .disableCachingNullValues()

                // Redis key serializer
                .serializeKeysWith(
                        RedisSerializationContext
                                .SerializationPair
                                .fromSerializer(
                                        new StringRedisSerializer()
                                )
                )

                // Redis value serializer
                .serializeValuesWith(
                        RedisSerializationContext
                                .SerializationPair
                                .fromSerializer(
                                        RedisSerializer.json()
                                )
                );
    }

    @Bean
    public RedisCacheManager redisCacheManager(
            RedisConnectionFactory redisConnectionFactory,
            RedisCacheConfiguration redisCacheConfiguration) {

        /*
         * Required by Spring Data Redis 4.1.1
         */
        RedisCacheWriter cacheWriter =
                RedisCacheWriter
                        .nonLockingRedisCacheWriter(
                                redisConnectionFactory
                        );

        return RedisCacheManager
                .builder(cacheWriter)
                .cacheDefaults(
                        redisCacheConfiguration
                )
                .build();
    }
}