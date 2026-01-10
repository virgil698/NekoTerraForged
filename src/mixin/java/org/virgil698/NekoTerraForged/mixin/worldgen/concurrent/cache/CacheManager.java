package org.virgil698.NekoTerraForged.mixin.worldgen.concurrent.cache;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 缓存管理器
 * 管理所有缓存实例的生命周期
 * 移植自 ReTerraForged
 */
public class CacheManager {
    private static final List<Cache<?>> CACHES = Collections.synchronizedList(new LinkedList<>());

    /**
     * 创建缓存
     */
    public static <V extends ExpiringEntry> Cache<V> createCache(int capacity, long expireTime, long pollInterval, TimeUnit unit) {
        Cache<V> cache = new Cache<>(capacity, expireTime, pollInterval, unit);
        CACHES.add(cache);
        return cache;
    }

    /**
     * 清理所有缓存
     */
    public static void clear() {
        for (Cache<?> cache : CACHES) {
            cache.close();
        }
        CACHES.clear();
    }

    /**
     * 获取缓存数量
     */
    public static int getCacheCount() {
        return CACHES.size();
    }
}
