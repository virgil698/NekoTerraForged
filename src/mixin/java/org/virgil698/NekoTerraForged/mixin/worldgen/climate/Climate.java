package org.virgil698.NekoTerraForged.mixin.worldgen.climate;

import org.virgil698.NekoTerraForged.mixin.worldgen.GeneratorContext;
import org.virgil698.NekoTerraForged.mixin.worldgen.cell.Cell;
import org.virgil698.NekoTerraForged.mixin.worldgen.cell.CellPopulator;
import org.virgil698.NekoTerraForged.mixin.worldgen.continent.Continent;
import org.virgil698.NekoTerraForged.mixin.worldgen.heightmap.Levels;
import org.virgil698.NekoTerraForged.mixin.worldgen.noise.NoiseUtil;
import org.virgil698.NekoTerraForged.mixin.worldgen.noise.module.Noise;
import org.virgil698.NekoTerraForged.mixin.worldgen.noise.module.Noises;
import org.virgil698.NekoTerraForged.mixin.worldgen.terrain.TerrainType;

/**
 * 气候系统
 * 移植自 ReTerraForged
 */
public class Climate implements CellPopulator {
    private final Noise offsetX;
    private final Noise offsetZ;
    private final int offsetDistance;
    private final Levels levels;
    private final BiomeNoise biomeNoise;

    public Climate(Noise offsetX, Noise offsetZ, int offsetDistance, Levels levels, BiomeNoise biomeNoise) {
        this.offsetX = offsetX;
        this.offsetZ = offsetZ;
        this.offsetDistance = offsetDistance;
        this.levels = levels;
        this.biomeNoise = biomeNoise;
    }

    @Override
    public void apply(Cell cell, float x, float z) {
        this.biomeNoise.apply(cell, x, z, x, z, true);
        float edgeBlend = 0.4F;
        if (cell.height <= this.levels.water) {
            if (cell.terrain == TerrainType.COAST) {
                cell.terrain = TerrainType.SHALLOW_OCEAN;
            }
        } else if (cell.biomeRegionEdge < edgeBlend || cell.terrain == TerrainType.MOUNTAIN_CHAIN) {
            float modifier = 1.0F - NoiseUtil.map(cell.biomeRegionEdge, 0.0F, edgeBlend, edgeBlend);
            float distance = this.offsetDistance * modifier;
            float dx = this.offsetX.compute(x, z, 0) * distance;
            float dz = this.offsetZ.compute(x, z, 0) * distance;
            float ox = x;
            float oz = z;
            x += dx;
            z += dz;
            this.biomeNoise.apply(cell, x, z, ox, oz, false);
        }
    }

    public static Climate make(Continent continent, GeneratorContext context) {
        Levels levels = context.levels;

        // 简单的生物群系边缘变形噪声
        int warpScale = 150;
        int warpStrength = 80;

        Noise biomeEdgeShape = Noises.simplex(context.seed.next(), warpScale, 2);
        Noise offsetX = Noises.shiftSeed(biomeEdgeShape, context.seed.next());
        Noise offsetZ = Noises.shiftSeed(biomeEdgeShape, context.seed.next());

        BiomeNoise biomeNoise = new BiomeNoise(context.seed, continent, levels);

        return new Climate(offsetX, offsetZ, warpStrength, levels, biomeNoise);
    }
}
