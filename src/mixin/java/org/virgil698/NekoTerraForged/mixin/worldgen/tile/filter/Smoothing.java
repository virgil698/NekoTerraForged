package org.virgil698.NekoTerraForged.mixin.worldgen.tile.filter;

import org.virgil698.NekoTerraForged.mixin.worldgen.cell.Cell;
import org.virgil698.NekoTerraForged.mixin.worldgen.heightmap.Levels;
import org.virgil698.NekoTerraForged.mixin.worldgen.noise.NoiseUtil;
import org.virgil698.NekoTerraForged.mixin.worldgen.tile.Tile;

/**
 * 平滑过滤器，用于平滑地形
 * 移植自 ReTerraForged
 */
public record Smoothing(float smoothingRadius, float smoothingRate, Modifier modifier) implements Filter {

    @Override
    public void apply(Tile tile, int seedX, int seedZ, int iterations) {
        while (iterations-- > 0) {
            this.apply(tile);
        }
    }

    private void apply(Tile tile) {
        int radius = NoiseUtil.round(this.smoothingRadius + 0.5F);
        float radiusSq = this.smoothingRadius * this.smoothingRadius;

        int maxZ = tile.getBlockSize().total() - radius;
        int maxX = tile.getBlockSize().total() - radius;
        for (int z = radius; z < maxZ; ++z) {
            for (int x = radius; x < maxX; ++x) {
                Cell cell = tile.getCellRaw(x, z);
                if (!cell.erosionMask) {
                    float total = 0.0F;
                    float weights = 0.0F;
                    for (int dz = -radius; dz <= radius; ++dz) {
                        for (int dx = -radius; dx <= radius; ++dx) {
                            float dist2 = (float) (dx * dx + dz * dz);
                            if (dist2 <= radiusSq) {
                                int px = x + dx;
                                int pz = z + dz;
                                Cell neighbour = tile.getCellRaw(px, pz);
                                if (!neighbour.isAbsent()) {
                                    float value = neighbour.height;
                                    float weight = 1.0F - dist2 / radiusSq;
                                    total += value * weight;
                                    weights += weight;
                                }
                            }
                        }
                    }
                    if (weights > 0.0F) {
                        float dif = cell.height - total / weights;
                        cell.height -= this.modifier.modify(cell, dif * this.smoothingRate);
                    }
                }
            }
        }
    }

    public static Smoothing make(float smoothingRadius, float smoothingRate, Levels levels) {
        return new Smoothing(smoothingRadius, smoothingRate, 
            Modifier.range(levels.ground(1), levels.ground(120)).invert());
    }
}
