package org.virgil698.NekoTerraForged.mixin.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.virgil698.NekoTerraForged.mixin.bridge.RTFBridge;
import org.virgil698.NekoTerraForged.mixin.bridge.RTFBridgeManager;

import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.progress.LevelLoadListener;
import net.minecraft.world.level.storage.ServerLevelData;

/**
 * 注入 MinecraftServer 来设置出生点搜索中心
 * 在 Leaves 1.21.10 中，setInitialSpawn 方法签名为:
 * setInitialSpawn(ServerLevel, ServerLevelData, boolean, boolean, LevelLoadListener)
 */
@Mixin(MinecraftServer.class)
public class MixinMinecraftServer {

    /**
     * 在查找出生点之前设置搜索中心
     * 注入点: Climate.Sampler.findSpawnPosition() 调用之前
     */
    @Inject(
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/biome/Climate$Sampler;findSpawnPosition()Lnet/minecraft/core/BlockPos;"
        ),
        method = "setInitialSpawn"
    )
    private static void ntf$beforeFindSpawnPosition(ServerLevel level, ServerLevelData levelData,
            boolean generateBonusChest, boolean debug, LevelLoadListener levelLoadListener, CallbackInfo ci) {
        RTFBridge bridge = RTFBridgeManager.INSTANCE.getBridge();
        if (bridge != null && bridge.isInitialized()) {
            // 获取 GeneratorContext 并计算出生点搜索中心
            Object contextObj = bridge.getGeneratorContext();
            if (contextObj != null) {
                // 默认使用原点作为搜索中心
                // 后续可以根据大陆生成器计算更合适的位置
                bridge.setSpawnSearchCenter(BlockPos.ZERO);
            }
        }
    }
}
