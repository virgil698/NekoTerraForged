package org.virgil698.NekoTerraForged.mixin.mixins;

import java.util.concurrent.atomic.AtomicBoolean;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.virgil698.NekoTerraForged.mixin.bridge.RTFBridge;
import org.virgil698.NekoTerraForged.mixin.bridge.RTFBridgeManager;
import org.virgil698.NekoTerraForged.mixin.worldgen.cell.CellField;
import org.virgil698.NekoTerraForged.mixin.worldgen.densityfunction.CellSampler;
import org.virgil698.NekoTerraForged.mixin.worldgen.densityfunction.NTFDensityFunctionVisitor;

import com.google.common.base.Suppliers;

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
    private final AtomicBoolean ntf$hasContext = new AtomicBoolean(false);
    
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
        
        // 检查是否启用 RTF
        RTFBridge bridge = RTFBridgeManager.INSTANCE.getBridge();
        boolean rtfEnabled = bridge != null && bridge.getConfig("worldgen.enabled", true);
        
        if (rtfEnabled) {
            // 强制设置 hasContext 为 true，启用 RTF 地形生成
            this.ntf$hasContext.set(true);
            System.out.println("[NekoTerraForged] RTF terrain generation enabled for seed: " + seed);
        } else {
            this.ntf$hasContext.set(false);
        }
        
        // 使用独立的类文件代替匿名/内部类，避免 Mixin 类加载问题
        this.ntf$densityFunctionWrapper = new NTFDensityFunctionVisitor(visitor, seed, this.ntf$hasContext);
        
        // 先应用 visitor 转换
        NoiseRouter mappedRouter = router.mapAll(this.ntf$densityFunctionWrapper);
        
        // 如果启用了 RTF，替换 temperature 和 vegetation 为 CellSampler
        if (rtfEnabled && bridge != null) {
            mappedRouter = ntf$replaceClimateFields(mappedRouter, bridge);
        }
        
        return mappedRouter;
    }
    
    /**
     * 替换 NoiseRouter 中的 temperature 和 vegetation 字段为 RTF 的 CellSampler
     */
    @Unique
    private NoiseRouter ntf$replaceClimateFields(NoiseRouter router, RTFBridge bridge) {
        try {
            // 创建 temperature CellSampler (使用 Cell 的 temperature 字段)
            CellSampler.Marker tempMarker = new CellSampler.Marker(CellField.TEMPERATURE);
            Object tempSampler = bridge.createCellSampler(tempMarker);
            DensityFunction temperature = tempSampler instanceof DensityFunction df ? df : router.temperature();
            
            // 创建 vegetation CellSampler (使用 Cell 的 moisture 字段)
            CellSampler.Marker vegMarker = new CellSampler.Marker(CellField.MOISTURE);
            Object vegSampler = bridge.createCellSampler(vegMarker);
            DensityFunction vegetation = vegSampler instanceof DensityFunction df ? df : router.vegetation();
            
            // 创建 continents CellSampler
            CellSampler.Marker contMarker = new CellSampler.Marker(CellField.CONTINENTALNESS);
            Object contSampler = bridge.createCellSampler(contMarker);
            DensityFunction continents = contSampler instanceof DensityFunction df ? df : router.continents();
            
            // 创建 erosion CellSampler
            CellSampler.Marker erosionMarker = new CellSampler.Marker(CellField.EROSION);
            Object erosionSampler = bridge.createCellSampler(erosionMarker);
            DensityFunction erosion = erosionSampler instanceof DensityFunction df ? df : router.erosion();
            
            // 创建 ridges CellSampler
            CellSampler.Marker ridgesMarker = new CellSampler.Marker(CellField.WEIRDNESS);
            Object ridgesSampler = bridge.createCellSampler(ridgesMarker);
            DensityFunction ridges = ridgesSampler instanceof DensityFunction df ? df : router.ridges();
            
            System.out.println("[NekoTerraForged] Replacing NoiseRouter fields with CellSampler");
            
            return new NoiseRouter(
                router.barrierNoise(),
                router.fluidLevelFloodednessNoise(),
                router.fluidLevelSpreadNoise(),
                router.lavaNoise(),
                temperature,
                vegetation,
                continents,
                erosion,
                router.depth(),
                ridges,
                router.preliminarySurfaceLevel(),
                router.finalDensity(),
                router.veinToggle(),
                router.veinRidged(),
                router.veinGap()
            );
        } catch (Exception e) {
            System.err.println("[NekoTerraForged] Failed to replace climate fields: " + e.getMessage());
            e.printStackTrace();
        }
        return router;
    }

    /**
     * 初始化 RTF 上下文
     * 在 MinecraftServer 或 ChunkMap 初始化时调用
     */
    @Unique
    public void ntf$initialize(RegistryAccess registryAccess) {
        this.ntf$registryAccess = registryAccess;
        
        if (this.ntf$hasContext.get()) {
            RTFBridge bridge = RTFBridgeManager.INSTANCE.getBridge();
            if (bridge != null) {
                bridge.initializeContext(registryAccess, this.ntf$seed);
                System.out.println("[NekoTerraForged] GeneratorContext initialized for seed: " + this.ntf$seed);
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
        return this.ntf$hasContext.get();
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
