package org.virgil698.NekoTerraForged.mixin.worldgen.tile;

import org.virgil698.NekoTerraForged.mixin.worldgen.cell.Cell;
import org.virgil698.NekoTerraForged.mixin.worldgen.heightmap.Heightmap;
import org.virgil698.NekoTerraForged.mixin.worldgen.rivermap.Rivermap;
import org.virgil698.NekoTerraForged.mixin.worldgen.tile.Tile.Chunk;
import org.virgil698.NekoTerraForged.mixin.worldgen.tile.filter.WorldFilters;

/**
 * Tile 生成器
 * 移植自 ReTerraForged
 */
public class TileGenerator {
    private final ThreadLocal<Heightmap> heightmap;
    private final WorldFilters filters;
    private final int tileChunks;
    private final int tileBorder;
    private final Size tileSizeBlocks;
    private final Size tileSizeChunks;

    public TileGenerator(ThreadLocal<Heightmap> heightmap, WorldFilters filters, int tileChunks, int tileBorder, int batchCount) {
        this.heightmap = heightmap;
        this.filters = filters;
        this.tileChunks = tileChunks;
        this.tileBorder = tileBorder;
        this.tileSizeBlocks = Size.blocks(tileChunks, tileBorder);
        this.tileSizeChunks = Size.chunks(tileChunks, tileBorder);
    }

    public Tile generate(int tileX, int tileZ) {
        Tile tile = this.makeTile(tileX, tileZ);
        Heightmap hm = this.heightmap.get();

        // 生成地形数据
        for (int cZ = 0; cZ < this.tileSizeChunks.total(); cZ++) {
            for (int cX = 0; cX < this.tileSizeChunks.total(); cX++) {
                Chunk chunk = tile.getChunkWriter(cX, cZ);
                Rivermap rivers = null;

                for (int dz = 0; dz < 16; dz++) {
                    for (int dx = 0; dx < 16; dx++) {
                        int worldX = chunk.getBlockX() + dx;
                        int worldZ = chunk.getBlockZ() + dz;
                        Cell cell = chunk.getCell(dx, dz);

                        hm.applyContinent(cell, worldX, worldZ);
                        rivers = Rivermap.get(cell, rivers, hm);
                        hm.applyTerrain(cell, worldX, worldZ, rivers);
                        hm.applyClimate(cell, worldX, worldZ);

                        chunk.updateHighestPoint(cell);
                    }
                }
            }
        }

        // 应用过滤器
        if (this.filters != null) {
            this.filters.apply(tile, true);
        }

        return tile;
    }

    private Tile makeTile(int x, int z) {
        return new Tile(x, z, this.tileChunks, this.tileBorder, this.tileSizeBlocks, this.tileSizeChunks);
    }
}
