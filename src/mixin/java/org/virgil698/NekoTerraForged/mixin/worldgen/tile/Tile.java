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

    /**
     * 根据世界方块坐标查找 Cell
     */
    public Cell lookup(int blockX, int blockZ) {
        // 计算相对于 Tile 起始位置的偏移
        int tileStartBlockX = getBlockX() - (this.border << 4);
        int tileStartBlockZ = getBlockZ() - (this.border << 4);
        
        int relBlockX = blockX - tileStartBlockX;
        int relBlockZ = blockZ - tileStartBlockZ;
        
        if (relBlockX < 0 || relBlockX >= this.blockSize.total() ||
            relBlockZ < 0 || relBlockZ >= this.blockSize.total()) {
            return Cell.empty();
        }
        
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
        // 计算相对于 Tile 的区块偏移
        int relChunkX = chunkX - this.chunkX + this.border;
        int relChunkZ = chunkZ - this.chunkZ + this.border;
        
        if (relChunkX < 0 || relChunkX >= this.chunkSize.total() ||
            relChunkZ < 0 || relChunkZ >= this.chunkSize.total()) {
            return new Chunk(relChunkX, relChunkZ);
        }
        
        int index = this.chunkSize.indexOf(relChunkX, relChunkZ);
        return this.computeChunk(index, relChunkX, relChunkZ);
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

        /**
         * 根据世界方块坐标获取 Cell
         */
        public Cell getCell(int blockX, int blockZ) {
            return Tile.this.lookup(blockX, blockZ);
        }

        public static int clampToNearestCell(int height, int cellHeight) {
            return (height / cellHeight + 1) * cellHeight;
        }
    }
}
