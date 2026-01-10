package org.virgil698.NekoTerraForged.mixin.worldgen.tile.filter;

import org.virgil698.NekoTerraForged.mixin.worldgen.heightmap.Heightmap;
import org.virgil698.NekoTerraForged.mixin.worldgen.heightmap.Levels;
import org.virgil698.NekoTerraForged.mixin.worldgen.tile.Size;
import org.virgil698.NekoTerraForged.mixin.worldgen.tile.Tile;

/**
 * 后处理过滤器
 * 移植自 ReTerraForged
 */
public record PostProcessing(Heightmap heightmap, Levels levels) implements Filter {

    @Override
    public void apply(Tile tile, int seedX, int seedZ, int iterations) {
        Size chunkSize = tile.getChunkSize();
        int chunkTotal = chunkSize.total();
        
        for (int chunkX = 0; chunkX < chunkTotal; chunkX++) {
            for (int chunkZ = 0; chunkZ < chunkTotal; chunkZ++) {
                Tile.Chunk chunk = tile.getChunkReader(chunkX, chunkZ);

                for (int dz = 0; dz < 16; dz++) {
                    for (int dx = 0; dx < 16; dx++) {
                        chunk.updateHighestPoint(chunk.getCell(dx, dz));
                    }
                }
            }
        }
    }
}
