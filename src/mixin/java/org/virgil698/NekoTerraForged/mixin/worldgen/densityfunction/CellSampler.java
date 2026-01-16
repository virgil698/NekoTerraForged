package org.virgil698.NekoTerraForged.mixin.worldgen.densityfunction;

import org.jetbrains.annotations.Nullable;
import org.virgil698.NekoTerraForged.mixin.worldgen.GeneratorContext;
import org.virgil698.NekoTerraForged.mixin.worldgen.cell.Cell;
import org.virgil698.NekoTerraForged.mixin.worldgen.cell.CellField;
import org.virgil698.NekoTerraForged.mixin.worldgen.heightmap.WorldLookup;
import org.virgil698.NekoTerraForged.mixin.worldgen.tile.Tile;
import org.virgil698.NekoTerraForged.mixin.worldgen.util.PosUtil;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.QuartPos;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.DensityFunction;

import java.util.function.Supplier;

/**
 * Cell 采样器密度函数
 * 移植自 ReTerraForged
 * 
 * 注意：MC 的密度函数使用 quart 坐标（4 方块为单位），
 * 但 RTF 的 Cell 系统使用方块坐标，需要正确转换
 */
public class CellSampler implements DensityFunction {
    private static final ThreadLocal<Cache2d> LOCAL_CELL = ThreadLocal.withInitial(Cache2d::new);
    private static final int CACHED_NOISE_CHUNK_SIZE = 16 / 4; // cellCountXZ for cached chunks

    private final Supplier<GeneratorContext> generatorContext;
    private final CellField field;

    public CellSampler(Supplier<GeneratorContext> generatorContext, CellField field) {
        this.generatorContext = generatorContext;
        this.field = field;
    }

    public CellField field() {
        return field;
    }

    /**
     * 检查是否是缓存的 NoiseChunk 大小
     */
    public static boolean isCachedNoiseChunk(int cellCountXZ) {
        return cellCountXZ == CACHED_NOISE_CHUNK_SIZE;
    }

    @Override
    public double compute(FunctionContext ctx) {
        GeneratorContext context = this.generatorContext.get();
        if (context == null) {
            return 0.0;
        }
        WorldLookup worldLookup = context.getLookup();
        if (worldLookup == null) {
            return 0.0;
        }
        
        // ctx.blockX/Z 已经是方块坐标，直接使用
        int blockX = ctx.blockX();
        int blockZ = ctx.blockZ();
        
        Cell cell = LOCAL_CELL.get().getAndUpdate(worldLookup, blockX, blockZ);
        return this.field.read(cell);
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
        return -1.0;
    }

    @Override
    public double maxValue() {
        return 1.0;
    }

    @Override
    public KeyDispatchDataCodec<? extends DensityFunction> codec() {
        throw new UnsupportedOperationException("CellSampler does not support codec");
    }

    /**
     * 2D 缓存 - 使用方块坐标
     */
    public static class Cache2d {
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

        public void reset() {
            this.lastPos = Long.MAX_VALUE;
            this.cell.reset();
        }
    }

    /**
     * 带区域缓存的 CellSampler
     * 用于 NoiseChunk 中的高效采样
     */
    @SuppressWarnings("unused")
    public class CacheChunk implements DensityFunction {
        @Nullable
        private final Tile.Chunk chunk;
        @Nullable
        private final Cache2d cache2d;
        private final int chunkX;
        private final int chunkZ;

        public CacheChunk(@Nullable Tile.Chunk chunk, @Nullable Cache2d cache2d, int chunkX, int chunkZ) {
            this.chunk = chunk;
            this.cache2d = cache2d;
            this.chunkX = chunkX;
            this.chunkZ = chunkZ;
        }

        @Override
        public double compute(FunctionContext ctx) {
            int blockX = ctx.blockX();
            int blockZ = ctx.blockZ();

            // 如果有 Tile.Chunk 缓存，直接从中读取
            if (this.chunk != null) {
                Cell cell = this.chunk.getCell(blockX, blockZ);
                if (cell != null && !cell.isAbsent()) {
                    return CellSampler.this.field.read(cell);
                }
            }

            // 否则使用 Cache2d
            if (this.cache2d != null) {
                GeneratorContext context = CellSampler.this.generatorContext.get();
                if (context != null) {
                    WorldLookup lookup = context.getLookup();
                    if (lookup != null) {
                        Cell cell = this.cache2d.getAndUpdate(lookup, blockX, blockZ);
                        return CellSampler.this.field.read(cell);
                    }
                }
            }

            // 回退到原始计算
            return CellSampler.this.compute(ctx);
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
            return CellSampler.this.minValue();
        }

        @Override
        public double maxValue() {
            return CellSampler.this.maxValue();
        }

        @Override
        public KeyDispatchDataCodec<? extends DensityFunction> codec() {
            throw new UnsupportedOperationException("CacheChunk does not support codec");
        }
    }

    /**
     * 标记类，用于在 NoiseRouter 中标识需要替换的密度函数
     */
    public static class Marker implements DensityFunction.SimpleFunction {
        public static final MapCodec<Marker> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            CellField.CODEC.fieldOf("field").forGetter(Marker::field)
        ).apply(instance, Marker::new));

        private final CellField field;

        public Marker(CellField field) {
            this.field = field;
        }

        public CellField field() {
            return field;
        }

        @Override
        public double compute(FunctionContext ctx) {
            return 0.0;
        }

        @Override
        public double minValue() {
            return -1.0;
        }

        @Override
        public double maxValue() {
            return 1.0;
        }

        @Override
        public KeyDispatchDataCodec<Marker> codec() {
            return KeyDispatchDataCodec.of(MAP_CODEC);
        }
    }
}
