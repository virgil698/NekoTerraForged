package org.virgil698.NekoTerraForged.mixin.world;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;

/**
 * 生物群系映射器
 * 根据地形类型、温度和湿度选择合适的生物群系
 */
public class BiomeMapper {

    /**
     * 获取生物群系
     */
    public static ResourceKey<Biome> getBiome(TerrainType terrainType, double temperature, double moisture, int height) {
        // 海洋生物群系
        if (terrainType.isOcean()) {
            if (temperature < -0.3) {
                return Biomes.FROZEN_OCEAN;
            } else if (terrainType == TerrainType.DEEP_OCEAN) {
                return Biomes.DEEP_OCEAN;
            } else {
                return Biomes.OCEAN;
            }
        }

        // 海滩
        if (terrainType == TerrainType.BEACH) {
            if (temperature < -0.3) {
                return Biomes.SNOWY_BEACH;
            }
            return Biomes.BEACH;
        }

        // 高山生物群系
        if (height > 180 || terrainType == TerrainType.HIGH_MOUNTAINS) {
            if (temperature < -0.3) {
                return Biomes.FROZEN_PEAKS;
            } else if (temperature < 0.0) {
                return Biomes.SNOWY_SLOPES;
            } else {
                return Biomes.STONY_PEAKS;
            }
        }

        // 山地生物群系
        if (terrainType == TerrainType.MOUNTAINS) {
            if (temperature < -0.2) {
                return Biomes.SNOWY_SLOPES;
            } else if (moisture > 0.3) {
                return Biomes.GROVE;
            } else {
                return Biomes.MEADOW;
            }
        }

        // 丘陵生物群系
        if (terrainType == TerrainType.HILLS) {
            if (temperature < -0.3) {
                return Biomes.SNOWY_TAIGA;
            } else if (temperature < 0.0) {
                return Biomes.TAIGA;
            } else if (temperature > 0.5) {
                if (moisture < -0.3) {
                    return Biomes.SAVANNA;
                } else {
                    return Biomes.FOREST;
                }
            } else {
                if (moisture > 0.3) {
                    return Biomes.DARK_FOREST;
                } else {
                    return Biomes.FOREST;
                }
            }
        }

        // 平原生物群系
        if (temperature < -0.5) {
            return Biomes.SNOWY_PLAINS;
        } else if (temperature < -0.2) {
            return Biomes.TAIGA;
        } else if (temperature > 0.6) {
            if (moisture < -0.5) {
                return Biomes.DESERT;
            } else if (moisture < 0.0) {
                return Biomes.SAVANNA;
            } else {
                return Biomes.JUNGLE;
            }
        } else if (temperature > 0.3) {
            if (moisture < -0.3) {
                return Biomes.PLAINS;
            } else if (moisture > 0.3) {
                return Biomes.SWAMP;
            } else {
                return Biomes.FOREST;
            }
        } else {
            if (moisture < -0.3) {
                return Biomes.PLAINS;
            } else if (moisture > 0.3) {
                return Biomes.BIRCH_FOREST;
            } else {
                return Biomes.FOREST;
            }
        }
    }
}
