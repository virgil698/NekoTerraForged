package org.virgil698.NekoTerraForged.mixin.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.Util;

import org.virgil698.NekoTerraForged.mixin.worldgen.concurrent.ThreadPools;
import org.virgil698.NekoTerraForged.mixin.worldgen.concurrent.cache.Cache;

/**
 * Mixin 注入 Util 类
 * 在服务器关闭时清理 RTF 线程池
 * 移植自 ReTerraForged MixinUtil
 */
@Mixin(Util.class)
public class MixinUtil {

    @Inject(method = "shutdownExecutors", at = @At("TAIL"))
    private static void onShutdownExecutors(CallbackInfo callback) {
        // 关闭 RTF 线程池
        ThreadPools.shutdown();
        // 关闭缓存调度器
        Cache.shutdownScheduler();
    }
}
