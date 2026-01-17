package org.virgil698.NekoTerraForged.mixin.world;

import org.virgil698.NekoTerraForged.mixin.bridge.RTFBridgeImpl;
import org.virgil698.NekoTerraForged.mixin.math.Node;
import org.virgil698.NekoTerraForged.mixin.math.Spline;
import org.virgil698.NekoTerraForged.mixin.world.river.River;
import org.virgil698.NekoTerraForged.mixin.world.river.RiverGenerator;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.Heightmap;

/**
 * Valley 风格的区块填充器
 * 基于 Valley 的多层噪声系统生成地形
 */
public class ValleyChunkFiller {
    private static final BlockState STONE = Blocks.STONE.defaultBlockState();
    private static final BlockState WATER = Blocks.WATER.defaultBlockState();
    private static final BlockState AIR = Blocks.AIR.defaultBlockState();
    
    private static final int SEA_LEVEL = 63;
    private static final int MIN_Y = -64;
    private static final int MAX_Y = 320;
    
    /**
     * 填充区块
     */
    public static void fillChunk(ChunkAccess chunk, RTFBridgeImpl bridge) {
        if (!bridge.isInitialized()) {
            return;
        }
        
        TerrainGenerator terrainGen = bridge.getTerrainGenerator();
        if (terrainGen == null) {
            return;
        }
        
        int seed = (int) bridge.getSeed();
        int chunkX = chunk.getPos().getMinBlockX();
        int chunkZ = chunk.getPos().getMinBlockZ();
        
        int minY = chunk.getMinY();
        int maxY = chunk.getMaxY();
        
        // 获取河流系统
        Node continentNode = bridge.getContinentNode();
        River.Config riverConfig = bridge.getRiverConfig();
        River.RegionCache riverCache = bridge.getRiverCache();
        Spline riverOutputSpline = bridge.getRiverOutputSpline();
        
        // 获取高度图
        Heightmap oceanFloor = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.OCEAN_FLOOR_WG);
        Heightmap worldSurface = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.WORLD_SURFACE_WG);
        
        // 遍历区块内的每个 X-Z 列
        for (int localX = 0; localX < 16; localX++) {
            for (int localZ = 0; localZ < 16; localZ++) {
                int worldX = chunkX + localX;
                int worldZ = chunkZ + localZ;
                
                // 使用地形生成器采样
                TerrainGenerator.TerrainData terrainData = terrainGen.sample(seed, worldX, worldZ);
                
                // 计算河流影响
                double riverDist = RiverGenerator.Sample(seed, worldX, worldZ, continentNode, riverConfig, riverCache);
                double riverSign = RiverGenerator.Sign(seed, worldX, worldZ);
                double riverValue = riverSign * riverOutputSpline.eval(riverDist);
                
                // 组合地形和河流
                double finalValue = terrainData.finalValue + riverValue * 0.2;
                
                // 计算最终高度
                int surfaceHeight = calculateHeight(finalValue, terrainData.terrainType);
                
                // 限制在有效范围内
                surfaceHeight = Math.max(minY, Math.min(maxY - 1, surfaceHeight));
                
                // 填充该列
                fillColumn(chunk, localX, localZ, surfaceHeight, minY, maxY, oceanFloor, worldSurface);
            }
        }
    }
    
    /**
     * 根据最终值和地形类型计算地表高度
     */
    private static int calculateHeight(double value, TerrainType terrainType) {
        // 基础高度映射
        int baseHeight;
        
        if (value <= -0.19) {
            // 海洋区域: -1.5 到 -0.19 映射到 -64 到 63
            double t = (value + 1.5) / (1.5 - 0.19);
            t = Math.max(0.0, Math.min(1.0, t));
            baseHeight = (int) (MIN_Y + t * (SEA_LEVEL - MIN_Y));
        } else {
            // 陆地区域: -0.19 到 1.5 映射到 63 到 250
            double t = (value + 0.19) / (1.5 + 0.19);
            t = Math.max(0.0, Math.min(1.0, t));
            baseHeight = (int) (SEA_LEVEL + t * 187);
        }
        
        // 根据地形类型微调
        switch (terrainType) {
            case BEACH:
                baseHeight = Math.max(SEA_LEVEL - 2, Math.min(SEA_LEVEL + 3, baseHeight));
                break;
            case HIGH_MOUNTAINS:
                baseHeight += 20; // 高山额外增高
                break;
            default:
                break;
        }
        
        return baseHeight;
    }
    
    /**
     * 填充一列方块
     */
    private static void fillColumn(
            ChunkAccess chunk,
            int localX,
            int localZ,
            int surfaceHeight,
            int minY,
            int maxY,
            Heightmap oceanFloor,
            Heightmap worldSurface) {
        
        // 从下到上填充
        for (int y = minY; y < maxY; y++) {
            BlockState state;
            
            if (y <= surfaceHeight) {
                // 固体地形
                state = STONE;
            } else if (y <= SEA_LEVEL) {
                // 水
                state = WATER;
            } else {
                // 空气 - 跳过
                continue;
            }
            
            // 设置方块
            int sectionIndex = chunk.getSectionIndex(y);
            LevelChunkSection section = chunk.getSection(sectionIndex);
            
            if (section != null) {
                int localY = y & 15; // y % 16
                section.setBlockState(localX, localY, localZ, state, false);
            }
        }
        
        // 更新高度图
        int waterLevel = Math.max(surfaceHeight, SEA_LEVEL);
        oceanFloor.update(localX, surfaceHeight, localZ, STONE);
        worldSurface.update(localX, waterLevel, localZ, waterLevel > surfaceHeight ? WATER : STONE);
    }
}
