package org.virgil698.NekoTerraForged.mixin.worldgen.concurrent.cache;

/**
 * 安全关闭接口
 * 不抛出异常的 AutoCloseable
 * 移植自 ReTerraForged
 */
public interface SafeCloseable extends AutoCloseable {
    
    @Override
    void close();
}
