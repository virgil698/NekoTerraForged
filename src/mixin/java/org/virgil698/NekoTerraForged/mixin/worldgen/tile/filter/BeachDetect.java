package org.virgil698.NekoTerraForged.mixin.worldgen.tile.filter;

import org.virgil698.NekoTerraForged.mixin.worldgen.cell.Cell;
import org.virgil698.NekoTerraForged.mixin.worldgen.heightmap.Levels;
import org.virgil698.NekoTerraForged.mixin.worldgen.terrain.TerrainType;
import org.virgil698.NekoTerraForged.mixin.worldgen.tile.Size;
import org.virgil698.NekoTerraForged.mixin.worldgen.tile.Tile;

/**
 * 海滩检测过滤器
 * 移植自 ReTerraForged
 */
public record BeachDetect(Levels levels, float beachTransition) implements Filter {

    @Override
    public void apply(Tile tile, int seedX, int seedZ, int iterations) {
        Size size = tile.getBlockSize();
        int total = size.total();

        for (int x = 0; x < total; x++) {
            for (int z = 0; z < total; z++) {
                Cell cell = tile.getCellRaw(x, z);
                if (cell.terrain.isCoast() && !cell.terrain.isWetland() && cell.continentEdge < this.beachTransition) {
                    Cell n = tile.getCellRaw(x, z - 8);
                    Cell s = tile.getCellRaw(x, z + 8);
                    Cell e = tile.getCellRaw(x + 8, z);
                    Cell w = tile.getCellRaw(x - 8, z);
                    float gx = this.grad(e, w, cell);
                    float gz = this.grad(n, s, cell);
                    float d2 = gx * gx + gz * gz;
                    if (d2 < 0.275F) {
                        tile.getCellRaw(x, z).terrain = TerrainType.BEACH;
                    }
                }
            }
        }
    }

    private float grad(Cell a, Cell b, Cell def) {
        int distance = 17;
        if (a.isAbsent()) {
            a = def;
            distance -= 8;
        }
        if (b.isAbsent()) {
            b = def;
            distance -= 8;
        }
        return (a.height - b.height) / distance;
    }

    public static BeachDetect make(Levels levels, float beachTransition) {
        return new BeachDetect(levels, beachTransition);
    }
}
