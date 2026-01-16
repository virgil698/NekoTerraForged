package org.virgil698.NekoTerraForged.mixin.worldgen.densityfunction;

import org.virgil698.NekoTerraForged.mixin.bridge.RTFBridge;
import org.virgil698.NekoTerraForged.mixin.bridge.RTFBridgeManager;
import org.virgil698.NekoTerraForged.mixin.worldgen.GeneratorContext;
import org.virgil698.NekoTerraForged.mixin.worldgen.cell.Cell;
import org.virgil698.NekoTerraForged.mixin.worldgen.heightmap.Levels;
import org.virgil698.NekoTerraForged.mixin.worldgen.heightmap.WorldLookup;
import org.virgil698.NekoTerraForged.mixin.worldgen.util.PosUtil;

import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.DensityFunction;

/**
 * RTF 偏移密度函数
 * 替换原版的 overworld/offset 函数
 * 
 * 原版公式: GLOBAL_OFFSET + spline(continentalness, erosion, ridges_folded)
 * RTF 公式: GLOBAL_OFFSET - 0.5 + (clampedHeight * 2.0)
 * 
 * 这个函数控制地形的基础高度
 */
public class RTFOffsetFunction implements DensityFunction {
    private static final float GLOBAL_OFFSET = -0.50375F;
    private static final int DEFAULT_TERRAIN_SCALER = 128;
    
    private final DensityFunction fallback;
    
    // 线程本地缓存
    private final ThreadLocal<Cache> cache = ThreadLocal.withInitial(Cache::new);
    
    public RTFOffsetFunction(DensityFunction fallback) {
        this.fallback = fallback;
    }
    
    // 调试日志计数器
    private static final java.util.concurrent.atomic.AtomicInteger debugCounter = new java.util.concurrent.atomic.AtomicInteger(0);
    
    @Override
    public double compute(FunctionContext ctx) {
        RTFBridge bridge = RTFBridgeManager.INSTANCE.getBridge();
        if (bridge == null || !bridge.isInitialized()) {
            return fallback.compute(ctx);
        }
        
        GeneratorContext context = (GeneratorContext) bridge.getGeneratorContext();
        if (context == null) {
            return fallback.compute(ctx);
        }
        
        WorldLookup lookup = context.getLookup();
        if (lookup == null) {
            return fallback.compute(ctx);
        }
        
        int blockX = ctx.blockX();
        int blockZ = ctx.blockZ();
        
        // 获取该位置的 Cell 数据
        Cache c = cache.get();
        Cell cell = c.getAndUpdate(lookup, blockX, blockZ);
        
        // 计算偏移量
        // 公式: GLOBAL_OFFSET - 0.5 + (clampedHeight * 2.0)
        int terrainScaler = context.levels != null ? context.levels.terrainScaler : DEFAULT_TERRAIN_SCALER;
        double clampedHeight = clampToNearestUnit(cell.height, terrainScaler);
        double result = GLOBAL_OFFSET - 0.5 + (clampedHeight * 2.0);
        
        // 每 10000 次采样输出一次调试信息
        int count = debugCounter.incrementAndGet();
        if (count % 10000 == 0) {
            System.out.println("[RTFOffset] x=" + blockX + " z=" + blockZ + 
                " height=" + String.format("%.4f", cell.height) + 
                " clamped=" + String.format("%.4f", clampedHeight) + 
                " offset=" + String.format("%.4f", result) +
                " temp=" + String.format("%.4f", cell.temperature) +
                " terrain=" + cell.terrain);
        }
        
        return result;
    }
    
    /**
     * 将值钳制到最近的单位 (来自 ClampToNearestUnit)
     */
    private double clampToNearestUnit(double value, int resolution) {
        float scaled = (int) (value * resolution) + 1;
        return scaled / resolution;
    }
    
    @Override
    public void fillArray(double[] array, ContextProvider contextProvider) {
        contextProvider.fillAllDirectly(array, this);
    }
    
    @Override
    public DensityFunction mapAll(Visitor visitor) {
        return visitor.apply(this);
    }
    
    @Override
    public double minValue() {
        return -64.0;
    }
    
    @Override
    public double maxValue() {
        return 64.0;
    }
    
    @Override
    public KeyDispatchDataCodec<? extends DensityFunction> codec() {
        throw new UnsupportedOperationException("RTFOffsetFunction does not support codec");
    }
    
    /**
     * 2D 缓存
     */
    private static class Cache {
        private long lastPos = Long.MAX_VALUE;
        private final Cell cell = new Cell();
        
        public Cell getAndUpdate(WorldLookup lookup, int blockX, int blockZ) {
            long packedPos = PosUtil.pack(blockX, blockZ);
            if (this.lastPos != packedPos) {
                this.cell.reset();
                lookup.apply(this.cell, blockX, blockZ);
                this.lastPos = packedPos;
            }
            return this.cell;
        }
    }
}
