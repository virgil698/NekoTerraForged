package org.virgil698.NekoTerraForged.mixin.worldgen.concurrent.cache;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.LongFunction;

/**
 * 带过期功能的缓存
 * 移植自 ReTerraForged
 */
public class Cache<V extends ExpiringEntry> implements AutoCloseable {
    public static final ScheduledExecutorService SCHEDULER = Executors.newSingleThreadScheduledExecutor((r) -> {
        Thread thread = new Thread(r);
        thread.setName("RTF-CacheScheduler");
        thread.setDaemon(true);
        return thread;
    });

    private final ConcurrentHashMap<Long, V> map;
    private final long lifetimeMS;
    private volatile long timeout;
    private final ScheduledFuture<?> poll;

    public Cache(int capacity, long expireTime, long pollInterval, TimeUnit unit) {
        this.timeout = 0L;
        this.map = new ConcurrentHashMap<>(capacity);
        this.lifetimeMS = unit.toMillis(expireTime);

        long intervalMillis = unit.toMillis(pollInterval);
        this.poll = SCHEDULER.scheduleAtFixedRate(this::poll, intervalMillis, intervalMillis, TimeUnit.MILLISECONDS);
    }

    public void remove(long key) {
        V entry = this.map.remove(key);
        if (entry != null) {
            entry.close();
        }
    }

    public V get(long key) {
        return this.map.get(key);
    }

    public V computeIfAbsent(long key, LongFunction<V> func) {
        return this.map.computeIfAbsent(key, func::apply);
    }

    public void poll() {
        this.timeout = System.currentTimeMillis() - this.lifetimeMS;
        this.map.entrySet().removeIf(entry -> {
            if (entry.getValue().getTimestamp() < this.timeout) {
                entry.getValue().close();
                return true;
            }
            return false;
        });
    }

    @Override
    public void close() {
        this.poll.cancel(false);
        this.map.values().forEach(ExpiringEntry::close);
        this.map.clear();
    }

    /**
     * 关闭调度器
     */
    public static void shutdownScheduler() {
        SCHEDULER.shutdown();
    }
}
