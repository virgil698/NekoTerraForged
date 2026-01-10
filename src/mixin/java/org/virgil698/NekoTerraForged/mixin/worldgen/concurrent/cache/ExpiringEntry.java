package org.virgil698.NekoTerraForged.mixin.worldgen.concurrent.cache;

/**
 * 可过期的缓存条目接口
 * 移植自 ReTerraForged
 */
public interface ExpiringEntry extends AutoCloseable {
    
    /**
     * 获取条目的时间戳
     */
    long getTimestamp();

    @Override
    default void close() {
        // 默认空实现
    }
}
