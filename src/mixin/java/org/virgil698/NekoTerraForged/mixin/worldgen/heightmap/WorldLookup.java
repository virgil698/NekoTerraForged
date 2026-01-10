package org.virgil698.NekoTerraForged.mixin.worldgen.heightmap;

import org.virgil698.NekoTerraForged.mixin.worldgen.GeneratorContext;
import org.virgil698.NekoTerraForged.mixin.worldgen.cell.Cell;
import org.virgil698.NekoTerraForged.mixin.worldgen.terrain.TerrainType;

import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;

/**
 * 世界查找器，用于获取指定坐标的地形数据
 * 移植自 ReTerraForged
 */
public class WorldLookup {
    private final GeneratorContext context;
    private final float waterLevel;
    private final float beachLevel;
    private final Levels levels;
    private final Heightmap heightmap;

    // 线程本地 Cell 缓存用于高度计算
    private final ThreadLocal<Cell> heightCell = ThreadLocal.withInitial(Cell::new);

    public WorldLookup(GeneratorContext context, Heightmap heightmap) {
        this.context = context;
        this.heightmap = heightmap;
        this.waterLevel = context.levels.water;
        this.beachLevel = context.levels.water(5);
        this.levels = context.levels;
    }

    public WorldLookup(GeneratorContext context) {
        this(context, context.getHeightmap());
    }

    public boolean apply(Cell cell, int x, int z) {
        return this.compute(cell, x, z);
    }

    public boolean compute(Cell cell, int x, int z) {
        heightmap.applyContinent(cell, x, z);
        heightmap.applyTerrain(cell, x, z);
        heightmap.applyClimate(cell, x, z);

        if (cell.terrain == TerrainType.COAST && cell.height > this.waterLevel && cell.height <= this.beachLevel) {
            cell.terrain = TerrainType.BEACH;
        }

        heightmap.applyPost(cell, x, z);
        return true;
    }

    public Levels getLevels() {
        return levels;
    }

    /**
     * 获取指定区块的生成高度
     * @param chunkX 区块 X 坐标
     * @param chunkZ 区块 Z 坐标
     * @param settings 噪声生成器设置
     * @param useCache 是否使用缓存
     * @return 生成高度
     */
    public int getGenerationHeight(int chunkX, int chunkZ, NoiseGeneratorSettings settings, boolean useCache) {
        int defaultHeight = settings.noiseSettings().height();
        
        // 计算区块中心的世界坐标
        int blockX = (chunkX << 4) + 8;
        int blockZ = (chunkZ << 4) + 8;
        
        // 获取该位置的高度
        Cell cell = heightCell.get().reset();
        this.apply(cell, blockX, blockZ);
        
        // 根据地形高度计算生成高度
        // 使用 levels 将归一化高度转换为实际方块高度
        int terrainHeight = (int) (cell.height * levels.worldHeight) + levels.minY;
        
        // 添加一些余量用于地形特征
        int generationHeight = Math.min(defaultHeight, terrainHeight + 64);
        
        // 确保至少有最小高度
        return Math.max(generationHeight, 128);
    }

    /**
     * 获取 GeneratorContext
     */
    public GeneratorContext getContext() {
        return context;
    }
}
