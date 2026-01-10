package org.virgil698.NekoTerraForged.mixin.worldgen.tile;

import java.util.Arrays;

import org.virgil698.NekoTerraForged.mixin.worldgen.cell.Cell;

/**
 * Tile 数据块，用于缓存地形数据
 * 移植自 ReTerraForged
 */
public class Tile implements AutoCloseable {
    private int x, z;
    private int chunkX, chunkZ;
    private int size;
    private int border;
    private Size blockSize;
    private Size chunkSize;
    private Cell[] cache;
    private Chunk[] chunks;

    public Tile(int x, int z, int size, int border, Size blockSize, Size chunkSize) {
        this.x = x;
        this.z = z;
        this.chunkX = x << size;
        this.chunkZ = z << size;
        this.size = size;
        this.border = border;
        this.blockSize = blockSize;
        this.chunkSize = chunkSize;
        this.cache = new Cell[blockSize.arraySize()];
        this.chunks = new Chunk[chunkSize.arraySize()];
        for (int i = 0; i < this.cache.length; i++) {
            this.cache[i] = new Cell();
        }
    }

    public int getX() {
        return this.x;
    }

    public int getZ() {
        return this.z;
    }

    public Cell lookup(int blockX, int blockZ) {
        int border = this.blockSize.border();
        int relBlockX = border + this.blockSize.mask(blockX);
        int relBlockZ = border + this.blockSize.mask(blockZ);
        int index = this.blockSize.indexOf(relBlockX, relBlockZ);
        if (index < 0 || index >= this.cache.length) {
            return Cell.empty();
        }
        return this.cache[index];
    }

    public Chunk getChunkWriter(int chunkX, int chunkZ) {
        int index = this.chunkSize.indexOf(chunkX, chunkZ);
        return this.computeChunk(index, chunkX, chunkZ);
    }

    public Chunk getChunkReader(int chunkX, int chunkZ) {
        int relChunkX = this.chunkSize.border() + this.chunkSize.mask(chunkX);
        int relChunkZ = this.chunkSize.border() + this.chunkSize.mask(chunkZ);
        int index = this.chunkSize.indexOf(relChunkX, relChunkZ);
        return this.computeChunk(index, chunkX, chunkZ);
    }

    public void iterate(Cell.Visitor visitor) {
        for (int dz = 0; dz < this.blockSize.size(); ++dz) {
            int z = this.blockSize.border() + dz;
            for (int dx = 0; dx < this.blockSize.size(); ++dx) {
                int x = this.blockSize.border() + dx;
                int index = this.blockSize.indexOf(x, z);
                Cell cell = this.cache[index];
                visitor.visit(cell, dx, dz);
            }
        }
    }

    public int getBlockX() {
        return Size.chunkToBlock(this.chunkX);
    }

    public int getBlockZ() {
        return Size.chunkToBlock(this.chunkZ);
    }

    public Size getBlockSize() {
        return this.blockSize;
    }

    public Size getChunkSize() {
        return this.chunkSize;
    }

    public Cell[] getBacking() {
        return this.cache;
    }

    public Cell getCellRaw(int x, int z) {
        int index = this.blockSize.indexOf(x, z);
        if (index < 0 || index >= this.blockSize.arraySize()) {
            return Cell.empty();
        }
        return this.cache[index];
    }

    @Override
    public void close() {
        for (Cell cell : this.cache) {
            cell.reset();
        }
        Arrays.fill(this.chunks, null);
    }

    private Chunk computeChunk(int index, int chunkX, int chunkZ) {
        if (index < 0 || index >= this.chunks.length) {
            return new Chunk(chunkX, chunkZ);
        }
        Chunk chunk = this.chunks[index];
        if (chunk == null) {
            chunk = new Chunk(chunkX, chunkZ);
            this.chunks[index] = chunk;
        }
        return chunk;
    }

    public class Chunk {
        private int chunkX;
        private int chunkZ;
        private int blockX;
        private int blockZ;
        private int regionBlockX;
        private int regionBlockZ;
        private float highestPoint;

        public Chunk(int regionChunkX, int regionChunkZ) {
            this.regionBlockX = regionChunkX << 4;
            this.regionBlockZ = regionChunkZ << 4;
            this.chunkX = Tile.this.chunkX + regionChunkX - Tile.this.border;
            this.chunkZ = Tile.this.chunkZ + regionChunkZ - Tile.this.border;
            this.blockX = this.chunkX << 4;
            this.blockZ = this.chunkZ << 4;
            this.highestPoint = Float.MIN_VALUE;
        }

        public void updateHighestPoint(Cell cell) {
            if (cell.height > this.highestPoint) {
                this.highestPoint = cell.height;
            }
        }

        public float getHighestPoint() {
            return this.highestPoint;
        }

        public int getChunkX() {
            return this.chunkX;
        }

        public int getChunkZ() {
            return this.chunkZ;
        }

        public int getBlockX() {
            return this.blockX;
        }

        public int getBlockZ() {
            return this.blockZ;
        }

        public Cell getCell(int blockX, int blockZ) {
            int relX = this.regionBlockX + (blockX & 0xF);
            int relZ = this.regionBlockZ + (blockZ & 0xF);
            int index = Tile.this.blockSize.indexOf(relX, relZ);
            if (index < 0 || index >= Tile.this.cache.length) {
                return Cell.empty();
            }
            return Tile.this.cache[index];
        }

        public static int clampToNearestCell(int height, int cellHeight) {
            return (height / cellHeight + 1) * cellHeight;
        }
    }
}
