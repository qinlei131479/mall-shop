package com.tigshop.common.config;
 
import com.tigshop.common.exception.GlobalException;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

/**
 * Redis配置类
 *
 * @author Tigshop团队
 * @create 2024年11月15日 13:28
 */
@Configuration
@EnableCaching
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(factory);

        // 使用 StringRedisSerializer 来序列化键
        redisTemplate.setKeySerializer(new StringRedisSerializer());

        // 使用 GenericJackson2JsonRedisSerializer 来序列化值（以 JSON 格式存储对象）
        redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());

        // 使用 StringRedisSerializer 来序列化哈希键
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());

        // 使用 Jackson2JsonRedisSerializer 来序列化哈希值
        redisTemplate.setHashValueSerializer(new Jackson2JsonRedisSerializer<>(Object.class));

        return redisTemplate;
    }

    @Bean
    public RedisCacheManager redisCacheManager(RedisTemplate<String, Object> redisTemplate) {
        // 确保 RedisTemplate 和连接工厂非空
        if (redisTemplate == null || redisTemplate.getConnectionFactory() == null) {
            throw new GlobalException("无法连接到redis服务");
        }

        // 使用非阻塞的 RedisCacheWriter
        RedisCacheWriter redisCacheWriter = RedisCacheWriter.nonLockingRedisCacheWriter(redisTemplate.getConnectionFactory());

        // 配置缓存的过期时间和不缓存 null 值
        RedisCacheConfiguration cacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                // 缓存过期时间
                .entryTtl(Duration.ofDays(15))
                // 不缓存 null 值
                .disableCachingNullValues();

        return new RedisCacheManager(redisCacheWriter, cacheConfig);
    }
}