package org.virgil698.NekoTerraForged.mixin.worldgen.tile;

/**
 * Tile 工厂接口
 * 移植自 ReTerraForged
 */
public interface TileFactory {
    Tile provide(int tileX, int tileZ);
    
    void queue(int tileX, int tileZ);
    
    void drop(int tileX, int tileZ);

    int chunkToTile(int chunkCoord);
    
    default Tile provideAtChunk(int chunkX, int chunkZ) {
        return this.provide(this.chunkToTile(chunkX), this.chunkToTile(chunkZ));
    }

    default void queueAtChunk(int chunkX, int chunkZ) {
        this.queue(this.chunkToTile(chunkX), this.chunkToTile(chunkZ));
    }

    default void dropAtChunk(int chunkX, int chunkZ) {
        this.drop(this.chunkToTile(chunkX), this.chunkToTile(chunkZ));
    }
    
    default Tile.Chunk provideChunk(int chunkX, int chunkZ) {
        return this.provideAtChunk(chunkX, chunkZ).getChunkReader(chunkX, chunkZ);
    }
}
