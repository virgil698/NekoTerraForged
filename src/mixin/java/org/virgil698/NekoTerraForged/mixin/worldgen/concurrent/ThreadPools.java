package org.virgil698.NekoTerraForged.mixin.worldgen.concurrent;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 线程池管理
 * 移植自 ReTerraForged
 */
public class ThreadPools {
    public static final ExecutorService WORLD_GEN = Executors.newFixedThreadPool(availableProcessors());

    public static int availableProcessors() {
        return Math.max(2, Runtime.getRuntime().availableProcessors());
    }

    /**
     * 关闭线程池
     */
    public static void shutdown() {
        WORLD_GEN.shutdown();
    }
}
