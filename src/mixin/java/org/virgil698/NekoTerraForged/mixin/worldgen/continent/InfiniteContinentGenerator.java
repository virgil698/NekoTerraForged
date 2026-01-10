package org.virgil698.NekoTerraForged.mixin.worldgen.continent;

import org.virgil698.NekoTerraForged.mixin.worldgen.GeneratorContext;
import org.virgil698.NekoTerraForged.mixin.worldgen.cell.Cell;
import org.virgil698.NekoTerraForged.mixin.worldgen.cell.CellPopulator;
import org.virgil698.NekoTerraForged.mixin.worldgen.noise.module.Noise;
import org.virgil698.NekoTerraForged.mixin.worldgen.rivermap.Rivermap;
import org.virgil698.NekoTerraForged.mixin.worldgen.rivermap.gen.GenWarp;
import org.virgil698.NekoTerraForged.mixin.worldgen.rivermap.river.Network;

/**
 * 无限大陆生成器（无海洋）
 * 移植自 ReTerraForged
 */
public class InfiniteContinentGenerator implements Continent {
    private final GeneratorContext context;

    public InfiniteContinentGenerator(GeneratorContext context) {
        this.context = context;
    }

    @Override
    public void apply(Cell cell, float x, float z) {
        // 无限大陆模式，始终为陆地
        cell.continentNoise = 1.0F;
        cell.continentEdge = 1.0F;
        cell.continentId = 0.0F;
        cell.continentX = 0;
        cell.continentZ = 0;
    }

    @Override
    public float getEdgeValue(float x, float z) {
        return 1.0F;
    }

    @Override
    public long getNearestCenter(float x, float z) {
        return 0L;
    }

    @Override
    public Rivermap getRivermap(int x, int z) {
        return new Rivermap(x, z, new Network[0], GenWarp.EMPTY);
    }

    @Override
    public CellPopulator mapNoise(Noise.Visitor visitor) {
        return this;
    }
}
