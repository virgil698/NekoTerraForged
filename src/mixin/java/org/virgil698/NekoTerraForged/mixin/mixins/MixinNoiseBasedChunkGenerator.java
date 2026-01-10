package org.virgil698.NekoTerraForged.mixin.mixins;

import java.util.List;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.virgil698.NekoTerraForged.mixin.bridge.RTFBridge;
import org.virgil698.NekoTerraForged.mixin.bridge.RTFBridgeManager;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.NoiseSettings;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;

/**
 * 注入 NoiseBasedChunkGenerator 来处理表面生成和调试信息
 * 在 Leaves 1.21.10 中，buildSurface 方法签名为:
 * buildSurface(WorldGenRegion, StructureManager, RandomState, ChunkAccess)
 */
@Mixin(value = NoiseBasedChunkGenerator.class, priority = 900)
public class MixinNoiseBasedChunkGenerator {

    @Shadow
    @Final
    public Holder<NoiseGeneratorSettings> settings;

    /**
     * 在 buildSurface 开始时设置 SurfaceRegion
     */
    @Inject(at = @At("HEAD"), method = "buildSurface(Lnet/minecraft/server/level/WorldGenRegion;Lnet/minecraft/world/level/StructureManager;Lnet/minecraft/world/level/levelgen/RandomState;Lnet/minecraft/world/level/chunk/ChunkAccess;)V")
    public void ntf$onBuildSurfaceHead(WorldGenRegion region, StructureManager structureManager,
            RandomState randomState, ChunkAccess chunk, CallbackInfo ci) {
        RTFBridge bridge = RTFBridgeManager.INSTANCE.getBridge();
        if (bridge != null) {
            bridge.setSurfaceRegion(region);
        }
    }

    /**
     * 在 buildSurface 结束时清除 SurfaceRegion
     */
    @Inject(at = @At("TAIL"), method = "buildSurface(Lnet/minecraft/server/level/WorldGenRegion;Lnet/minecraft/world/level/StructureManager;Lnet/minecraft/world/level/levelgen/RandomState;Lnet/minecraft/world/level/chunk/ChunkAccess;)V")
    public void ntf$onBuildSurfaceTail(WorldGenRegion region, StructureManager structureManager,
            RandomState randomState, ChunkAccess chunk, CallbackInfo ci) {
        RTFBridge bridge = RTFBridgeManager.INSTANCE.getBridge();
        if (bridge != null) {
            bridge.setSurfaceRegion(null);
        }
    }

    /**
     * 重定向 fillFromNoise 中的 NoiseSettings.height() 调用
     * 根据 RTF 地形数据优化生成高度
     */
    @Redirect(
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/levelgen/NoiseSettings;height()I"
        ),
        method = "fillFromNoise"
    )
    public int ntf$fillFromNoise(NoiseSettings settings, Blender blender, RandomState randomState, 
            StructureManager structureManager, ChunkAccess chunk) {
        RTFBridge bridge = RTFBridgeManager.INSTANCE.getBridge();
        if (bridge != null && bridge.isInitialized()) {
            ChunkPos chunkPos = chunk.getPos();
            return bridge.getGenerationHeight(chunkPos.x, chunkPos.z, this.settings.value());
        }
        return settings.height();
    }

    /**
     * 添加调试屏幕信息
     */
    @Inject(at = @At("TAIL"), method = "addDebugScreenInfo")
    private void ntf$addDebugScreenInfo(List<String> info, RandomState randomState, BlockPos pos, CallbackInfo ci) {
        RTFBridge bridge = RTFBridgeManager.INSTANCE.getBridge();
        if (bridge != null && bridge.isInitialized()) {
            Object cellObj = bridge.applyCell(pos.getX(), pos.getZ());
            if (cellObj != null) {
                info.add("");
                info.add("[NekoTerraForged]");
                info.add("Height: " + String.format("%.3f", bridge.getHeight(pos.getX(), pos.getZ())));
                info.add("Continentalness: " + String.format("%.3f", bridge.getContinentalness(pos.getX(), pos.getZ())));
                info.add("Erosion: " + String.format("%.3f", bridge.getErosion(pos.getX(), pos.getZ())));
                info.add("Terrain Mask: " + String.format("%.3f", bridge.getCellField(pos.getX(), pos.getZ(), "TERRAIN_MASK")));
                info.add("River Distance: " + String.format("%.3f", bridge.getCellField(pos.getX(), pos.getZ(), "RIVER_DISTANCE")));
            }
        }
    }
}
