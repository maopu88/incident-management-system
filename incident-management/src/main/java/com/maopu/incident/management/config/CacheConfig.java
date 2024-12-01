package com.maopu.incident.management.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import com.google.common.base.Charsets;
import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import com.maopu.incident.management.utils.Constants;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManagerWithRandomTTL() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();

        // 配置 incidents 缓存
        Caffeine<Object, Object> incidentsCacheBuilder = Caffeine.newBuilder()
                // 设置最大容量为200个条目
                .maximumSize(200)
                .expireAfter(getExpiry());

        // 配置 incident 缓存
        Caffeine<Object, Object> incidentCacheBuilder = Caffeine.newBuilder()
                // 设置最大容量为1000个条目
                .maximumSize(1000)
                .expireAfter(getExpiry());

        // 创建缓存实例
        CaffeineCache incidentsCache = new CaffeineCache(Constants.CACHE_NAME_INCIDENTS, incidentsCacheBuilder.build());
        CaffeineCache incidentCache = new CaffeineCache(Constants.CACHE_NAME_INCIDENT, incidentCacheBuilder.build());

        // 添加到缓存管理器
        cacheManager.setCaches(Arrays.asList(incidentsCache, incidentCache));

        return cacheManager;
    }

    /**
     * 缓存雪崩是指大量的缓存在同一时间失效，导致大量请求直接访问数据库。可以通过设置随机过期时间来避免这种情况
     *
     * @return
     */
    private Expiry<Object, Object> getExpiry() {
        return new Expiry<Object, Object>() {
            @Override
            public long expireAfterCreate(Object key, Object value, long currentTime) {
                // 设置随机过期时间，范围在30分钟到60分钟之间
                return TimeUnit.MINUTES.toNanos(ThreadLocalRandom.current().nextLong(30, 60));
            }

            @Override
            public long expireAfterUpdate(Object key, Object value, long currentTime, long currentDuration) {
                return currentDuration;
            }

            @Override
            public long expireAfterRead(Object key, Object value, long currentTime, long currentDuration) {
                return currentDuration;
            }
        };
    }

    @Bean
    public BloomFilter<String> bloomFilter() {
        // 预期插入的数量
        int expectedInsertions = 5000;
        // 期望的误报率
        double fpp = 0.01;
        return BloomFilter.create(Funnels.stringFunnel(Charsets.UTF_8), expectedInsertions, fpp);
    }
}
