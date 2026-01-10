package org.virgil698.NekoTerraForged.mixin.worldgen.terrain.populator;

import org.virgil698.NekoTerraForged.mixin.worldgen.cell.Cell;
import org.virgil698.NekoTerraForged.mixin.worldgen.cell.CellPopulator;
import org.virgil698.NekoTerraForged.mixin.worldgen.noise.module.Noise;
import org.virgil698.NekoTerraForged.mixin.worldgen.terrain.Terrain;
import org.virgil698.NekoTerraForged.mixin.worldgen.terrain.region.RegionSelector;

/**
 * 地形填充器
 * 移植自 ReTerraForged
 */
public class TerrainPopulator implements CellPopulator, RegionSelector.Weighted {
    private final Terrain terrain;
    private final Noise ground;
    private final Noise height;
    private final Noise erosion;
    private final Noise weirdness;
    private final float weight;

    public TerrainPopulator(Terrain terrain, Noise ground, Noise height, Noise erosion, Noise weirdness, float weight) {
        this.terrain = terrain;
        this.ground = ground;
        this.height = height;
        this.erosion = erosion;
        this.weirdness = weirdness;
        this.weight = weight;
    }

    public Terrain type() {
        return this.terrain;
    }

    public Noise height() {
        return this.height;
    }

    public Noise weirdness() {
        return this.weirdness;
    }

    @Override
    public void apply(Cell cell, float x, float z) {
        float groundHeight = this.ground.compute(x, z, 0);
        float terrainHeight = this.height.compute(x, z, 0);
        
        cell.terrain = this.terrain;
        cell.height = groundHeight + terrainHeight;
        cell.erosion = this.erosion.compute(x, z, 0);
        cell.weirdness = this.weirdness.compute(x, z, 0);
    }

    @Override
    public float weight() {
        return this.weight;
    }

    @Override
    public CellPopulator mapNoise(Noise.Visitor visitor) {
        return new TerrainPopulator(
            this.terrain,
            this.ground.mapAll(visitor),
            this.height.mapAll(visitor),
            this.erosion.mapAll(visitor),
            this.weirdness.mapAll(visitor),
            this.weight
        );
    }

    public static TerrainPopulator make(Terrain terrain, Noise ground, Noise height, Noise erosion, Noise weirdness, float weight) {
        return new TerrainPopulator(terrain, ground, height, erosion, weirdness, weight);
    }
}
