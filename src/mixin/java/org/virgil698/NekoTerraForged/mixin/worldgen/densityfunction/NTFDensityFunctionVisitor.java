package org.virgil698.NekoTerraForged.mixin.worldgen.densityfunction;

import java.util.concurrent.atomic.AtomicBoolean;

import org.virgil698.NekoTerraForged.mixin.bridge.RTFBridge;
import org.virgil698.NekoTerraForged.mixin.bridge.RTFBridgeManager;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;

/**
 * DensityFunction.Visitor 实现
 * 单独的类文件，避免 Mixin 匿名/内部类导致的 NoClassDefFoundError
 * 
 * 核心功能：
 * 1. 拦截 RTF 的 Marker 密度函数，替换为实际的 Sampler
 * 2. 拦截 overworld/offset 函数，替换为 RTF 的 offset 函数
 */
public final class NTFDensityFunctionVisitor implements DensityFunction.Visitor {
    private final DensityFunction.Visitor delegate;
    private final long seed;
    private final AtomicBoolean hasContext;
    private final boolean replaceVanilla;
    
    public NTFDensityFunctionVisitor(DensityFunction.Visitor delegate, long seed, AtomicBoolean hasContext) {
        this.delegate = delegate;
        this.seed = seed;
        this.hasContext = hasContext;
        
        // 检查是否启用替换原版密度函数
        RTFBridge bridge = RTFBridgeManager.INSTANCE.getBridge();
        this.replaceVanilla = bridge != null && bridge.getConfig("worldgen.enabled", true);
    }
    
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
            hasContext.set(true);
            RTFBridge bridge = RTFBridgeManager.INSTANCE.getBridge();
            if (bridge != null) {
                Object cellSampler = bridge.createCellSampler(function);
                if (cellSampler instanceof DensityFunction df) {
                    return df;
                }
            }
        }
        
        // 如果启用了替换原版密度函数，检查是否是 HolderHolder 类型
        if (replaceVanilla && className.contains("HolderHolder")) {
            DensityFunction replaced = tryReplaceVanillaFunction(function);
            if (replaced != null) {
                hasContext.set(true);
                return replaced;
            }
        }
        
        return delegate.apply(function);
    }
    
    /**
     * 尝试替换原版密度函数
     */
    private DensityFunction tryReplaceVanillaFunction(DensityFunction function) {
        if (function instanceof DensityFunctions.HolderHolder holderHolder) {
            Holder<DensityFunction> holder = holderHolder.function();
            if (holder instanceof Holder.Reference<DensityFunction> ref) {
                ResourceKey<DensityFunction> key = ref.key();
                String path = key.location().getPath();
                
                // 替换 offset 函数 - 这是控制地形高度的关键
                if (path.equals("overworld/offset") || path.equals("overworld_large_biomes/offset")) {
                    // 使用 RTF 的 offset 函数，它会在运行时检查 GeneratorContext
                    RTFOffsetFunction rtfOffset = new RTFOffsetFunction(function);
                    System.out.println("[NekoTerraForged] Replaced vanilla offset function: " + path);
                    return rtfOffset;
                }
            }
        }
        return null;
    }

    @Override
    public DensityFunction.NoiseHolder visitNoise(DensityFunction.NoiseHolder noiseHolder) {
        return delegate.visitNoise(noiseHolder);
    }
}
