package com.incidentIQ.user_service.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.incidentIQ.user_service.constant.CacheNames;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.*;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(
            RedisConnectionFactory connectionFactory) {

        RedisTemplate<String, Object> template =
                new RedisTemplate<>();

        template.setConnectionFactory(connectionFactory);

        ObjectMapper objectMapper = new ObjectMapper();

        objectMapper.activateDefaultTyping(
                BasicPolymorphicTypeValidator.builder().build(),
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );

        GenericJackson2JsonRedisSerializer serializer =
                new GenericJackson2JsonRedisSerializer(objectMapper);

        template.setKeySerializer(
                new StringRedisSerializer());

        template.setValueSerializer(serializer);

        template.setHashKeySerializer(
                new StringRedisSerializer());

        template.setHashValueSerializer(serializer);

        template.afterPropertiesSet();

        return template;
    }

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {

        RedisCacheConfiguration defaultConfig =
                RedisCacheConfiguration.defaultCacheConfig()
                        .serializeKeysWith(
                                RedisSerializationContext.SerializationPair.fromSerializer(
                                        new StringRedisSerializer()))
                        .serializeValuesWith(
                                RedisSerializationContext.SerializationPair.fromSerializer(
                                        new GenericJackson2JsonRedisSerializer()));

        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

        cacheConfigurations.put(
                CacheNames.USERS_BY_EMAIL,
                defaultConfig.entryTtl(Duration.ofMinutes(30)));

        cacheConfigurations.put(
                CacheNames.USERS_BY_ID,
                defaultConfig.entryTtl(Duration.ofMinutes(30)));

        cacheConfigurations.put(
                CacheNames.ADMINS,
                defaultConfig.entryTtl(Duration.ofHours(1)));

        cacheConfigurations.put(
                CacheNames.SUPPORT_ENGINEERS,
                defaultConfig.entryTtl(Duration.ofHours(1)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .transactionAware()
                .build();
    }
}