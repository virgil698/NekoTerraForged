package org.virgil698.NekoTerraForged.mixin.worldgen.surface;

import net.minecraft.server.level.WorldGenRegion;

/**
 * 表面生成区域的线程本地存储
 * 移植自 ReTerraForged
 */
public class SurfaceRegion {
    private static final ThreadLocal<WorldGenRegion> THREAD_LOCAL = new ThreadLocal<>();

    public static void set(WorldGenRegion region) {
        THREAD_LOCAL.set(region);
    }

    public static WorldGenRegion get() {
        return THREAD_LOCAL.get();
    }
}
