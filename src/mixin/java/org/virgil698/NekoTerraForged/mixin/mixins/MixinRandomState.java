package org.virgil698.NekoTerraForged.mixin.mixins;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.virgil698.NekoTerraForged.mixin.bridge.RTFBridge;
import org.virgil698.NekoTerraForged.mixin.bridge.RTFBridgeManager;

import net.minecraft.core.HolderGetter;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.NoiseRouter;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.SurfaceSystem;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

/**
 * 注入 RandomState 来处理 RTF 密度函数
 * 在 Leaves 1.21.10 中，RandomState 构造函数签名为:
 * RandomState(NoiseGeneratorSettings settings, HolderGetter<NormalNoise.NoiseParameters> noiseParametersGetter, long levelSeed)
 * 
 * 参考 ReTerraForged MixinRandomState 实现
 */
@Mixin(RandomState.class)
public class MixinRandomState {
    @Shadow
    @Final
    private Climate.Sampler sampler;
    
    @Shadow
    @Final
    private SurfaceSystem surfaceSystem;

    @Unique
    @Nullable
    private RegistryAccess ntf$registryAccess;
    
    @Unique
    private long ntf$seed;
    
    @Unique
    private boolean ntf$hasContext;
    
    @Unique
    private DensityFunction.Visitor ntf$densityFunctionWrapper;

    /**
     * 重定向 NoiseRouter.mapAll 调用来拦截密度函数处理
     * 这是 RTF 的核心注入点，用于替换 CellSampler.Marker 和 NoiseSampler.Marker
     */
    @Redirect(
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/levelgen/NoiseRouter;mapAll(Lnet/minecraft/world/level/levelgen/DensityFunction$Visitor;)Lnet/minecraft/world/level/levelgen/NoiseRouter;"
        ),
        method = "<init>",
        require = 1
    )
    private NoiseRouter ntf$onInit(NoiseRouter router, DensityFunction.Visitor visitor, 
            NoiseGeneratorSettings settings, HolderGetter<NormalNoise.NoiseParameters> params, final long seed) {
        this.ntf$seed = seed;
        this.ntf$hasContext = false;
        
        this.ntf$densityFunctionWrapper = new DensityFunction.Visitor() {
            @Override
            public DensityFunction apply(DensityFunction function) {
                String className = function.getClass().getName();
                
                // 检查是否是 NoiseSampler.Marker
                if (className.contains("NoiseSampler$Marker")) {
                    RTFBridge bridge = RTFBridgeManager.INSTANCE.getBridge();
                    if (bridge != null) {
                        Object noiseSampler = bridge.createNoiseSampler(function, (int) seed);
                        if (noiseSampler instanceof DensityFunction df) {
                            return df;
                        }
                    }
                }
                
                // 检查是否是 CellSampler.Marker
                if (className.contains("CellSampler$Marker")) {
                    MixinRandomState.this.ntf$hasContext = true;
                    RTFBridge bridge = RTFBridgeManager.INSTANCE.getBridge();
                    if (bridge != null) {
                        Object cellSampler = bridge.createCellSampler(function);
                        if (cellSampler instanceof DensityFunction df) {
                            return df;
                        }
                    }
                }
                
                return visitor.apply(function);
            }

            @Override
            public DensityFunction.NoiseHolder visitNoise(DensityFunction.NoiseHolder noiseHolder) {
                return visitor.visitNoise(noiseHolder);
            }
        };
        return router.mapAll(this.ntf$densityFunctionWrapper);
    }

    /**
     * 初始化 RTF 上下文
     * 在 MinecraftServer 或 ChunkMap 初始化时调用
     */
    @Unique
    public void ntf$initialize(RegistryAccess registryAccess) {
        this.ntf$registryAccess = registryAccess;
        
        if (this.ntf$hasContext) {
            RTFBridge bridge = RTFBridgeManager.INSTANCE.getBridge();
            if (bridge != null) {
                bridge.initializeContext(registryAccess, this.ntf$seed);
            }
        }
    }

    @Unique
    @Nullable
    public RegistryAccess ntf$registryAccess() {
        return this.ntf$registryAccess;
    }

    @Unique
    @Nullable
    public Object ntf$generatorContext() {
        RTFBridge bridge = RTFBridgeManager.INSTANCE.getBridge();
        return bridge != null ? bridge.getGeneratorContext() : null;
    }

    @Unique
    public long ntf$seed() {
        return this.ntf$seed;
    }
    
    @Unique
    public boolean ntf$hasContext() {
        return this.ntf$hasContext;
    }

    /**
     * 包装密度函数，应用 RTF 转换
     */
    @Unique
    @Nullable
    public DensityFunction ntf$wrap(DensityFunction function) {
        if (this.ntf$densityFunctionWrapper != null) {
            return function.mapAll(this.ntf$densityFunctionWrapper);
        }
        return function;
    }
}
