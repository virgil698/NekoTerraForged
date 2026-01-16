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

        // 计算 Tile 的起始方块坐标
        int tileBlockX = tile.getBlockX();
        int tileBlockZ = tile.getBlockZ();

        // 生成地形数据 - 遍历整个 Tile 的方块
        int totalBlocks = this.tileSizeBlocks.total();
        Cell[] backing = tile.getBacking();
        
        for (int relZ = 0; relZ < totalBlocks; relZ++) {
            int worldZ = tileBlockZ + relZ - (this.tileBorder << 4);
            
            for (int relX = 0; relX < totalBlocks; relX++) {
                int worldX = tileBlockX + relX - (this.tileBorder << 4);
                
                int index = this.tileSizeBlocks.indexOf(relX, relZ);
                if (index >= 0 && index < backing.length) {
                    Cell cell = backing[index];
                    cell.reset();
                    
                    // 应用地形生成
                    hm.applyContinent(cell, worldX, worldZ);
                    Rivermap rivers = hm.continent().getRivermap(cell);
                    hm.applyTerrain(cell, worldX, worldZ, rivers);
                    hm.applyClimate(cell, worldX, worldZ);
                }
            }
        }

        // 更新每个 Chunk 的最高点
        for (int cZ = 0; cZ < this.tileSizeChunks.total(); cZ++) {
            for (int cX = 0; cX < this.tileSizeChunks.total(); cX++) {
                Chunk chunk = tile.getChunkWriter(cX, cZ);
                for (int dz = 0; dz < 16; dz++) {
                    for (int dx = 0; dx < 16; dx++) {
                        Cell cell = chunk.getCell(chunk.getBlockX() + dx, chunk.getBlockZ() + dz);
                        if (cell != null && !cell.isAbsent()) {
                            chunk.updateHighestPoint(cell);
                        }
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
