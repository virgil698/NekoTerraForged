package org.virgil698.NekoTerraForged.mixin.worldgen.tile;

import java.util.concurrent.ConcurrentHashMap;

import org.jetbrains.annotations.Nullable;
import org.virgil698.NekoTerraForged.mixin.worldgen.util.PosUtil;

/**
 * Tile 缓存
 * 移植自 ReTerraForged
 */
public class TileCache {
    private int tileSize;
    private ConcurrentHashMap<Long, Tile> cache;
    private TileGenerator generator;

    public TileCache(int tileSize, TileGenerator generator) {
        this.tileSize = tileSize;
        this.cache = new ConcurrentHashMap<>();
        this.generator = generator;
    }

    public TileGenerator getGenerator() {
        return this.generator;
    }

    @Nullable
    public Tile provideIfPresent(int tileX, int tileZ) {
        return this.cache.get(PosUtil.pack(tileX, tileZ));
    }

    @Nullable
    public Tile provideAtChunkIfPresent(int chunkX, int chunkZ) {
        return this.provideIfPresent(this.chunkToTile(chunkX), this.chunkToTile(chunkZ));
    }

    public Tile provide(int tileX, int tileZ) {
        return this.cache.computeIfAbsent(PosUtil.pack(tileX, tileZ), key -> {
            return this.generator.generate(tileX, tileZ);
        });
    }

    public Tile provideAtChunk(int chunkX, int chunkZ) {
        return this.provide(this.chunkToTile(chunkX), this.chunkToTile(chunkZ));
    }

    /**
     * 预加载指定区块的 Tile
     * 用于结构生成前预缓存
     */
    public void queueAtChunk(int chunkX, int chunkZ) {
        // 预加载 Tile
        this.provideAtChunk(chunkX, chunkZ);
    }

    /**
     * 释放指定区块的 Tile
     * 用于特征生成后清理缓存
     */
    public void dropAtChunk(int chunkX, int chunkZ) {
        this.drop(this.chunkToTile(chunkX), this.chunkToTile(chunkZ));
    }

    public void drop(int tileX, int tileZ) {
        long key = PosUtil.pack(tileX, tileZ);
        Tile tile = this.cache.remove(key);
        if (tile != null) {
            tile.close();
        }
    }

    public int chunkToTile(int chunkCoord) {
        return chunkCoord >> this.tileSize;
    }

    public void clear() {
        for (Tile tile : this.cache.values()) {
            tile.close();
        }
        this.cache.clear();
    }
}
