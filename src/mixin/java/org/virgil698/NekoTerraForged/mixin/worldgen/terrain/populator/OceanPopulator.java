package org.virgil698.NekoTerraForged.mixin.worldgen.terrain.populator;

import org.virgil698.NekoTerraForged.mixin.worldgen.cell.Cell;
import org.virgil698.NekoTerraForged.mixin.worldgen.cell.CellPopulator;
import org.virgil698.NekoTerraForged.mixin.worldgen.noise.module.Noise;
import org.virgil698.NekoTerraForged.mixin.worldgen.terrain.Terrain;

/**
 * 海洋填充器
 * 移植自 ReTerraForged
 */
public class OceanPopulator implements CellPopulator {
    private final Terrain terrain;
    private final Noise height;

    public OceanPopulator(Terrain terrain, Noise height) {
        this.terrain = terrain;
        this.height = height;
    }

    @Override
    public void apply(Cell cell, float x, float z) {
        cell.terrain = this.terrain;
        cell.height = this.height.compute(x, z, 0);
    }

    @Override
    public CellPopulator mapNoise(Noise.Visitor visitor) {
        return new OceanPopulator(this.terrain, this.height.mapAll(visitor));
    }
}
