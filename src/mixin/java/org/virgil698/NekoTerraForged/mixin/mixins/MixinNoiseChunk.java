package org.virgil698.NekoTerraForged.mixin.mixins;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.virgil698.NekoTerraForged.mixin.bridge.RTFBridge;
import org.virgil698.NekoTerraForged.mixin.bridge.RTFBridgeManager;
import org.virgil698.NekoTerraForged.mixin.worldgen.NTFFluidPicker;

import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ColumnPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;
import net.minecraft.world.level.levelgen.NoiseChunk;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.NoiseSettings;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;

/**
 * 注入 NoiseChunk 来处理 CellSampler 的缓存
 * 在 Leaves 1.21.10 中，NoiseChunk 构造函数签名为:
 * NoiseChunk(int cellCountXZ, RandomState random, int firstNoiseX, int firstNoiseZ, 
 *            NoiseSettings noiseSettings, DensityFunctions.BeardifierOrMarker beardifier,
 *            NoiseGeneratorSettings noiseGeneratorSettings, Aquifer.FluidPicker fluidPicker, Blender blendifier)
 * 
 * 参考 ReTerraForged MixinNoiseChunk 实现
 * 
 * 注意：cellCountY 是 final 字段，在 Java 17+ 中不能从非 <init> 方法修改
 */
@Mixin(NoiseChunk.class)
public class MixinNoiseChunk {
    @Unique
    private RandomState ntf$randomState;
    
    @Unique
    private int ntf$chunkX;
    
    @Unique
    private int ntf$chunkZ;
    
    @Unique
    private int ntf$generationHeight;
    
    @Unique
    @Nullable
    private Object ntf$tileChunk;
    
    @Unique
    @Nullable
    private Object ntf$cache2d;
    
    @Unique
    private NoiseGeneratorSettings ntf$generatorSettings;

    @Shadow
    @Final
    int firstNoiseX;

    @Shadow
    @Final
    int firstNoiseZ;

    @Shadow
    @Final
    private int cellCountXZ;

    @Shadow
    @Final
    private int cellCountY;

    @Shadow
    @Final
    private int cellHeight;

    /**
     * 在构造函数末尾注入来初始化 RTF 上下文
     * 使用 @Inject 而不是 @Redirect 来避免 final 字段修改问题
     */
    @Inject(
        method = "<init>",
        at = @At("TAIL")
    )
    private void ntf$onInit(int cellCountXZ, RandomState randomState, 
            int minBlockX, int minBlockZ, NoiseSettings noiseSettings, 
            DensityFunctions.BeardifierOrMarker beardifierOrMarker, NoiseGeneratorSettings noiseGeneratorSettings,
            Aquifer.FluidPicker fluidPicker, Blender blender, CallbackInfo ci) {
        this.ntf$randomState = randomState;
        this.ntf$chunkX = SectionPos.blockToSectionCoord(minBlockX);
        this.ntf$chunkZ = SectionPos.blockToSectionCoord(minBlockZ);
        this.ntf$generatorSettings = noiseGeneratorSettings;
        
        RTFBridge bridge = RTFBridgeManager.INSTANCE.getBridge();
        if (bridge != null && bridge.isInitialized() && bridge.getGeneratorContext() != null) {
            // 获取生成高度
            this.ntf$generationHeight = bridge.getGenerationHeight(this.ntf$chunkX, this.ntf$chunkZ, noiseGeneratorSettings);
            
            // 创建缓存
            this.ntf$cache2d = bridge.createCache2d();
            
            // 获取 Tile.Chunk
            this.ntf$tileChunk = bridge.getTileChunk(this.ntf$chunkX, this.ntf$chunkZ);
        } else {
            this.ntf$generationHeight = noiseSettings.height();
        }
    }

    /**
     * 修改 FluidPicker 来支持自定义岩浆层高度
     * fluidPicker 是构造函数的第 8 个参数
     */
    @ModifyVariable(
        method = "<init>",
        at = @At("HEAD"),
        ordinal = 0,
        argsOnly = true
    )
    private static Aquifer.FluidPicker ntf$modifyFluidPicker(Aquifer.FluidPicker fluidPicker, 
            int cellCountXZ, RandomState randomState, int minBlockX, int minBlockZ, 
            NoiseSettings noiseSettings, DensityFunctions.BeardifierOrMarker beardifierOrMarker, 
            NoiseGeneratorSettings noiseGeneratorSettings, Aquifer.FluidPicker fluidPicker2, Blender blender) {
        RTFBridge bridge = RTFBridgeManager.INSTANCE.getBridge();
        if (bridge != null && bridge.isInitialized()) {
            int lavaLevel = bridge.getLavaLevel();
            if (lavaLevel != 0) {
                Aquifer.FluidStatus lava = new Aquifer.FluidStatus(lavaLevel, Blocks.LAVA.defaultBlockState());
                int seaLevel = noiseGeneratorSettings.seaLevel();
                Aquifer.FluidStatus defaultFluid = new Aquifer.FluidStatus(seaLevel, noiseGeneratorSettings.defaultFluid());
                // 使用独立的类文件代替 lambda，避免 Mixin 类加载问题
                return new NTFFluidPicker(lava, defaultFluid, lavaLevel, seaLevel);
            }
        }
        return fluidPicker;
    }

    /**
     * 拦截 wrapNew 方法来处理 CellSampler
     * 当遇到 CellSampler 时，返回带缓存的版本
     */
    @Inject(
        at = @At("HEAD"),
        method = "wrapNew",
        cancellable = true
    )
    private void ntf$wrapNew(DensityFunction function, CallbackInfoReturnable<DensityFunction> cir) {
        RTFBridge bridge = RTFBridgeManager.INSTANCE.getBridge();
        if (bridge != null && bridge.isInitialized()) {
            // 检查是否是 CellSampler (不是 Marker 或 CacheChunk)
            String className = function.getClass().getName();
            if (className.contains("CellSampler") && !className.contains("Marker") && !className.contains("CacheChunk")) {
                // 创建带缓存的 CellSampler
                Object cachedSampler = bridge.createCachedCellSampler(function, this.ntf$tileChunk, 
                        this.ntf$cache2d, this.ntf$chunkX, this.ntf$chunkZ);
                if (cachedSampler instanceof DensityFunction df) {
                    cir.setReturnValue(df);
                }
            }
        }
    }

    /**
     * 重定向 computePreliminarySurfaceLevel 中的 NoiseSettings.height() 调用
     * 根据 RTF 地形数据优化生成高度
     */
    @Redirect(
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/levelgen/NoiseSettings;height()I"
        ),
        require = 0,
        method = "computePreliminarySurfaceLevel"
    )
    private int ntf$computePreliminarySurfaceLevel(NoiseSettings settings, long packedPos) {
        int blockX = ColumnPos.getX(packedPos);
        int blockZ = ColumnPos.getZ(packedPos);
        
        RTFBridge bridge = RTFBridgeManager.INSTANCE.getBridge();
        if (bridge != null && bridge.isInitialized() && bridge.getGeneratorContext() != null) {
            return bridge.getGenerationHeight(
                SectionPos.blockToSectionCoord(blockX), 
                SectionPos.blockToSectionCoord(blockZ), 
                this.ntf$generatorSettings
            );
        }
        
        return this.ntf$generatorSettings.noiseSettings().height();
    }
}
