package org.virgil698.NekoTerraForged.mixin.worldgen.cell;

import org.virgil698.NekoTerraForged.mixin.worldgen.biome.type.BiomeType;
import org.virgil698.NekoTerraForged.mixin.worldgen.terrain.Terrain;
import org.virgil698.NekoTerraForged.mixin.worldgen.terrain.TerrainType;

/**
 * 单元格数据，存储地形生成过程中的各种参数
 * 移植自 ReTerraForged
 */
public class Cell {
    private static final Cell DEFAULTS = new Cell();
    private static final Cell EMPTY = new Cell() {
        @Override
        public boolean isAbsent() {
            return true;
        }
    };

    // 高度相关
    public float height;
    public float gradient;
    
    // 侵蚀相关
    public float localErosion;
    public float localErosion2;
    public float sediment;
    public float sediment2;
    public boolean erosionMask;
    public float erosion;
    
    // 气候相关
    public float regionMoisture;
    public float regionTemperature;
    public float temperature;
    public float moisture;
    public float weirdness;
    
    // 大陆相关
    public float continentId;
    public float continentNoise;
    public float continentEdge;
    public float continentalness;
    public int continentX;
    public int continentZ;
    
    // 地形区域相关
    public float terrainRegionId;
    public float terrainRegionEdge;
    public long terrainRegionCenter;
    public Terrain terrain;
    public float terrainMask;
    public float borderMask;
    public float mountainChainAlpha;
    
    // 生物群系相关
    public float biomeRegionId;
    public float biomeRegionEdge;
    public float macroBiomeId;
    public BiomeType biomeType;
    
    // 河流相关
    public float riverDistance;

    public Cell() {
        this.regionMoisture = 0.5F;
        this.regionTemperature = 0.5F;
        this.biomeRegionEdge = 1.0F;
        this.riverDistance = 1.0F;
        this.erosionMask = false;
        this.terrain = TerrainType.NONE;
        this.terrainMask = 1.0F;
        this.biomeType = BiomeType.GRASSLAND;
    }

    public void copyFrom(Cell other) {
        this.height = other.height;
        this.localErosion = other.localErosion;
        this.localErosion2 = other.localErosion2;
        this.sediment = other.sediment;
        this.sediment2 = other.sediment2;
        this.gradient = other.gradient;
        this.regionMoisture = other.regionMoisture;
        this.regionTemperature = other.regionTemperature;
        this.continentId = other.continentId;
        this.continentNoise = other.continentNoise;
        this.continentEdge = other.continentEdge;
        this.continentalness = other.continentalness;
        this.terrainRegionId = other.terrainRegionId;
        this.terrainRegionEdge = other.terrainRegionEdge;
        this.terrainRegionCenter = other.terrainRegionCenter;
        this.biomeRegionId = other.biomeRegionId;
        this.biomeRegionEdge = other.biomeRegionEdge;
        this.macroBiomeId = other.macroBiomeId;
        this.riverDistance = other.riverDistance;
        this.continentX = other.continentX;
        this.continentZ = other.continentZ;
        this.erosionMask = other.erosionMask;
        this.terrain = other.terrain;
        this.terrainMask = other.terrainMask;
        this.borderMask = other.borderMask;
        this.biomeType = other.biomeType;
        this.erosion = other.erosion;
        this.weirdness = other.weirdness;
        this.temperature = other.temperature;
        this.moisture = other.moisture;
        this.mountainChainAlpha = other.mountainChainAlpha;
    }

    public Cell reset() {
        this.copyFrom(DEFAULTS);
        return this;
    }

    public boolean isAbsent() {
        return false;
    }

    public static Cell empty() {
        return EMPTY;
    }

    @FunctionalInterface
    public interface Visitor {
        void visit(Cell cell, int x, int z);
    }
}
