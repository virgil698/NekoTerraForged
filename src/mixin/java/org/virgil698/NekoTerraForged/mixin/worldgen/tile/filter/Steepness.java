package org.virgil698.NekoTerraForged.mixin.worldgen.tile.filter;

import org.virgil698.NekoTerraForged.mixin.worldgen.cell.Cell;
import org.virgil698.NekoTerraForged.mixin.worldgen.heightmap.Levels;
import org.virgil698.NekoTerraForged.mixin.worldgen.tile.Tile;

/**
 * 陡峭度过滤器，计算地形梯度
 * 移植自 ReTerraForged
 */
public record Steepness(int radius, float scaler, float waterLevel) implements Filter, Filter.Visitor {

    @Override
    public void apply(Tile tile, int seedX, int seedZ, int iterations) {
        this.iterate(tile, this);
    }

    @Override
    public void visit(Tile tile, Cell cell, int cx, int cz) {
        float totalHeightDif = 0.0F;
        for (int dz = -1; dz <= 2; ++dz) {
            for (int dx = -1; dx <= 2; ++dx) {
                if (dx != 0 || dz != 0) {
                    int x = cx + dx * this.radius;
                    int z = cz + dz * this.radius;
                    Cell neighbour = tile.getCellRaw(x, z);
                    if (!neighbour.isAbsent()) {
                        float height = Math.max(neighbour.height, this.waterLevel);
                        totalHeightDif += Math.abs(cell.height - height) / this.radius;
                    }
                }
            }
        }
        cell.gradient = Math.min(1.0F, totalHeightDif * this.scaler);
    }

    public static Steepness make(int radius, float scaler, Levels levels) {
        return new Steepness(radius, scaler, levels.water);
    }
}
