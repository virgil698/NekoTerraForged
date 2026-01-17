package org.virgil698.NekoTerraForged.mixin.world;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 表面装饰器
 * 根据地形类型和高度添加表面方块
 */
public class SurfaceDecorator {
    private static final BlockState GRASS_BLOCK = Blocks.GRASS_BLOCK.defaultBlockState();
    private static final BlockState DIRT = Blocks.DIRT.defaultBlockState();
    private static final BlockState STONE = Blocks.STONE.defaultBlockState();
    private static final BlockState SAND = Blocks.SAND.defaultBlockState();
    private static final BlockState GRAVEL = Blocks.GRAVEL.defaultBlockState();
    private static final BlockState SNOW_BLOCK = Blocks.SNOW_BLOCK.defaultBlockState();
    private static final BlockState ICE = Blocks.ICE.defaultBlockState();

    /**
     * 获取表面方块
     */
    public static BlockState getSurfaceBlock(TerrainType terrainType, int height, double temperature) {
        // 高海拔 -> 雪
        if (height > 180 || temperature < -0.5) {
            return SNOW_BLOCK;
        }

        switch (terrainType) {
            case BEACH:
                return SAND;
            case PLAINS:
            case HILLS:
                return GRASS_BLOCK;
            case MOUNTAINS:
            case HIGH_MOUNTAINS:
                if (height > 140) {
                    return STONE;
                }
                return GRASS_BLOCK;
            default:
                return GRASS_BLOCK;
        }
    }

    /**
     * 获取次表面方块
     */
    public static BlockState getSubSurfaceBlock(TerrainType terrainType, int depth) {
        if (depth > 4) {
            return STONE;
        }

        switch (terrainType) {
            case BEACH:
                return depth < 3 ? SAND : STONE;
            case PLAINS:
            case HILLS:
                return depth < 3 ? DIRT : STONE;
            case MOUNTAINS:
            case HIGH_MOUNTAINS:
                return depth < 2 ? STONE : STONE;
            default:
                return DIRT;
        }
    }

    /**
     * 获取海底方块
     */
    public static BlockState getOceanFloorBlock(int depth) {
        if (depth < 5) {
            return SAND;
        } else if (depth < 10) {
            return GRAVEL;
        } else {
            return STONE;
        }
    }
}
