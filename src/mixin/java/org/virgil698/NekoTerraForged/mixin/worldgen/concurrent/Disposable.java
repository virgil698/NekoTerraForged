package org.virgil698.NekoTerraForged.mixin.worldgen.concurrent;

/**
 * 可释放资源接口
 * 移植自 ReTerraForged
 */
public interface Disposable {
    
    /**
     * 释放资源
     */
    void dispose();

    /**
     * 释放监听器
     */
    interface Listener<T> {
        void onDispose(T ctx);
    }
}
