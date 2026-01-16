package org.virgil698.NekoTerraForged.mixin.worldgen.densityfunction;

import org.virgil698.NekoTerraForged.mixin.worldgen.GeneratorContext;
import org.virgil698.NekoTerraForged.mixin.worldgen.cell.Cell;
import org.virgil698.NekoTerraForged.mixin.worldgen.heightmap.Levels;
import org.virgil698.NekoTerraForged.mixin.worldgen.heightmap.WorldLookup;
import org.virgil698.NekoTerraForged.mixin.worldgen.util.PosUtil;

import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.DensityFunction;

import java.util.function.Supplier;

/**
 * RTF 深度密度函数
 * 这是控制地形高度的核心函数
 * 
 * 原版 MC 的 depth 函数: yClampedGradient + offset
 * RTF 的 depth 函数: 基于 Cell.height 计算实际地形高度
 * 
 * 公式: depth = (terrainHeight - y) / scale
 * 当 depth > 0 时生成实心方块，depth < 0 时生成空气
 */
public class RTFDepthFunction implements DensityFunction {
    private static final float SCALER = 128.0F;
    private static final float GLOBAL_OFFSET = -0.50375F; // 来自 NoiseRouterData
    
    private final Supplier<GeneratorContext> contextSupplier;
    private final int minY;
    private final int maxY;
    private final int seaLevel;
    
    // 线程本地缓存
    private final ThreadLocal<Cache> cache = ThreadLocal.withInitial(Cache::new);
    
    public RTFDepthFunction(Supplier<GeneratorContext> contextSupplier, int minY, int maxY, int seaLevel) {
        this.contextSupplier = contextSupplier;
        this.minY = minY;
        this.maxY = maxY;
        this.seaLevel = seaLevel;
    }
    
    @Override
    public double compute(FunctionContext ctx) {
        int blockX = ctx.blockX();
        int blockY = ctx.blockY();
        int blockZ = ctx.blockZ();
        
        GeneratorContext context = contextSupplier.get();
        if (context == null) {
            // 回退到原版计算
            return computeVanilla(blockY);
        }
        
        WorldLookup lookup = context.getLookup();
        if (lookup == null) {
            return computeVanilla(blockY);
        }
        
        // 获取该位置的 Cell 数据
        Cache c = cache.get();
        Cell cell = c.getAndUpdate(lookup, blockX, blockZ);
        
        // 计算地形高度 (Cell.height 是 0-1 的归一化值)
        Levels levels = lookup.getLevels();
        int terrainHeight = levels.scale(cell.height) + levels.minY;
        
        // 计算 depth: 正值 = 实心，负值 = 空气
        // 使用与原版类似的梯度计算
        double yGradient = computeYGradient(blockY);
        double offset = computeOffset(cell.height, levels);
        
        return yGradient + offset;
    }
    
    /**
     * 计算 Y 梯度 (与原版 yClampedGradient 类似)
     */
    private double computeYGradient(int y) {
        if (y <= minY) {
            return yGradientRange(minY);
        }
        if (y >= maxY) {
            return yGradientRange(maxY);
        }
        // 线性插值
        double t = (double)(y - minY) / (maxY - minY);
        return yGradientRange(minY) + t * (yGradientRange(maxY) - yGradientRange(minY));
    }
    
    /**
     * 计算偏移量 (基于 RTF 的 HEIGHT)
     */
    private double computeOffset(float height, Levels levels) {
        // 将 height (0-1) 转换为偏移量
        // 公式来自 PresetNoiseRouterData:
        // offset = GLOBAL_OFFSET - 0.5 + (clampedHeight * 2.0)
        double clampedHeight = clampToNearestUnit(height, levels.terrainScaler);
        return GLOBAL_OFFSET - 0.5 + (clampedHeight * 2.0);
    }
    
    /**
     * 将值钳制到最近的单位 (来自 ClampToNearestUnit)
     */
    private double clampToNearestUnit(double value, int resolution) {
        float scaled = (int) (value * resolution) + 1;
        return scaled / resolution;
    }
    
    private static double yGradientRange(int y) {
        return 1.0 + (-y / SCALER);
    }
    
    /**
     * 原版深度计算 (回退用)
     */
    private double computeVanilla(int y) {
        double yGradient = computeYGradient(y);
        return yGradient + GLOBAL_OFFSET;
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
        throw new UnsupportedOperationException("RTFDepthFunction does not support codec");
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
