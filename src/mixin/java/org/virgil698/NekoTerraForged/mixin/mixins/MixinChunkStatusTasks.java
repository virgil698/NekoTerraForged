package org.virgil698.NekoTerraForged.mixin.mixins;

import java.util.concurrent.CompletableFuture;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.virgil698.NekoTerraForged.mixin.bridge.RTFBridge;
import org.virgil698.NekoTerraForged.mixin.bridge.RTFBridgeManager;

import net.minecraft.server.level.GenerationChunkHolder;
import net.minecraft.util.StaticCache2D;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatusTasks;
import net.minecraft.world.level.chunk.status.ChunkStep;
import net.minecraft.world.level.chunk.status.WorldGenContext;

/**
 * 注入 ChunkStatusTasks 来管理 RTF 的 Tile 缓存和 WorldGenFlags
 * 
 * 在 Leaves 1.21.10 中，ChunkStatus 的任务逻辑被移到了 ChunkStatusTasks 类中
 * 这个 Mixin 替代了原来的 MixinChunkStatus
 * 
 * 参考 ReTerraForged MixinChunkStatus 实现
 */
@Mixin(ChunkStatusTasks.class)
public class MixinChunkStatusTasks {

    /**
     * 在 generateStructureStarts 开始时，队列化 Tile 缓存并禁用快速 Cell 查找
     * 结构生成需要精确的地形数据
     */
    @Inject(
        at = @At("HEAD"),
        method = "generateStructureStarts"
    )
    private static void ntf$generateStructureStarts$HEAD(WorldGenContext worldGenContext, ChunkStep step, 
            StaticCache2D<GenerationChunkHolder> cache, ChunkAccess chunk, 
            CallbackInfoReturnable<CompletableFuture<ChunkAccess>> callback) {
        RTFBridge bridge = RTFBridgeManager.INSTANCE.getBridge();
        if (bridge != null && bridge.isInitialized() && bridge.getGeneratorContext() != null) {
            ChunkPos chunkPos = chunk.getPos();
            bridge.queueTileAtChunk(chunkPos.x, chunkPos.z);
            bridge.setFastCellLookups(false);
        }
    }

    /**
     * 在 generateStructureStarts 结束时，重新启用快速 Cell 查找
     */
    @Inject(
        at = @At("TAIL"),
        method = "generateStructureStarts"
    )
    private static void ntf$generateStructureStarts$TAIL(WorldGenContext worldGenContext, ChunkStep step, 
            StaticCache2D<GenerationChunkHolder> cache, ChunkAccess chunk, 
            CallbackInfoReturnable<CompletableFuture<ChunkAccess>> callback) {
        RTFBridge bridge = RTFBridgeManager.INSTANCE.getBridge();
        if (bridge != null && bridge.isInitialized() && bridge.getGeneratorContext() != null) {
            bridge.setFastCellLookups(true);
        }
    }

    /**
     * 在 generateFeatures 结束时，释放 Tile 缓存
     * 特征生成是最后一个需要地形数据的阶段
     */
    @Inject(
        at = @At("TAIL"),
        method = "generateFeatures"
    )
    private static void ntf$generateFeatures$TAIL(WorldGenContext worldGenContext, ChunkStep step, 
            StaticCache2D<GenerationChunkHolder> cache, ChunkAccess chunk, 
            CallbackInfoReturnable<CompletableFuture<ChunkAccess>> callback) {
        RTFBridge bridge = RTFBridgeManager.INSTANCE.getBridge();
        if (bridge != null && bridge.isInitialized() && bridge.getGeneratorContext() != null) {
            ChunkPos chunkPos = chunk.getPos();
            bridge.dropTileAtChunk(chunkPos.x, chunkPos.z);
        }
    }
}
