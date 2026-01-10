package org.virgil698.NekoTerraForged.mixin.worldgen.tile.filter;

import java.util.function.IntFunction;

import org.virgil698.NekoTerraForged.mixin.worldgen.GeneratorContext;
import org.virgil698.NekoTerraForged.mixin.worldgen.heightmap.Heightmap;
import org.virgil698.NekoTerraForged.mixin.worldgen.tile.Tile;

/**
 * 世界过滤器管理器
 * 移植自 ReTerraForged
 */
public class WorldFilters {
    private final Smoothing smoothing;
    private final Steepness steepness;
    private final BeachDetect beach;
    private final PostProcessing processing;
    private final WorldErosion<Erosion> erosion;
    private final int erosionIterations;
    private final int smoothingIterations;

    public WorldFilters(GeneratorContext context, Heightmap heightmap) {
        IntFunction<Erosion> factory = Erosion.factory(context);
        this.beach = BeachDetect.make(context.levels, 0.6f);
        this.smoothing = Smoothing.make(1.5f, 0.5f, context.levels);
        this.steepness = Steepness.make(1, 10.0F, context.levels);
        this.processing = new PostProcessing(heightmap, context.levels);
        this.erosion = new WorldErosion<>(factory, (e, size) -> e.getSize() == size);
        this.erosionIterations = Erosion.ErosionSettings.defaults().dropletsPerChunk;
        this.smoothingIterations = 2;
    }

    /**
     * 应用所有过滤器到 Tile
     * @param tile Tile 实例
     * @param optionalFilters 是否应用可选过滤器（如侵蚀）
     */
    public void apply(Tile tile, boolean optionalFilters) {
        int regionX = tile.getX();
        int regionZ = tile.getZ();

        if (optionalFilters) {
            this.applyOptionalFilters(tile, regionX, regionZ);
        }
        this.applyRequiredFilters(tile, regionX, regionZ);
        this.applyPostProcessing(tile, regionX, regionZ);
    }

    private void applyRequiredFilters(Tile tile, int seedX, int seedZ) {
        this.steepness.apply(tile, seedX, seedZ, 1);
        this.beach.apply(tile, seedX, seedZ, 1);
    }

    private void applyOptionalFilters(Tile tile, int seedX, int seedZ) {
        Erosion erosion = this.erosion.get(tile.getBlockSize().total());
        erosion.apply(tile, seedX, seedZ, this.erosionIterations);
        this.smoothing.apply(tile, seedX, seedZ, this.smoothingIterations);
    }

    public void applyPostProcessing(Tile map, int seedX, int seedZ) {
        this.processing.apply(map, seedX, seedZ, 1);
    }
}
