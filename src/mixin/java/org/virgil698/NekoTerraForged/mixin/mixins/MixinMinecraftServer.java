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
 */
@Mixin(MinecraftServer.class)
public class MixinMinecraftServer {

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
            // 默认使用原点作为搜索中心
            bridge.setSpawnSearchCenter(BlockPos.ZERO);
        }
    }
}
