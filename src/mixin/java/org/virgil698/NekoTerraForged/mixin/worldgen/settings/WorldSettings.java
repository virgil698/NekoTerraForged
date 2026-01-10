package org.virgil698.NekoTerraForged.mixin.worldgen.settings;

import org.virgil698.NekoTerraForged.mixin.worldgen.continent.ContinentType;
import org.virgil698.NekoTerraForged.mixin.worldgen.noise.function.DistanceFunction;

/**
 * 世界生成设置
 * 移植自 ReTerraForged
 */
public class WorldSettings {
    // 大陆设置
    public int continentScale = 3000;
    public float continentJitter = 0.7F;
    public DistanceFunction continentShape = DistanceFunction.EUCLIDEAN;
    public ContinentType continentType = ContinentType.MULTI;
    
    // 控制点
    public float deepOcean = 0.1F;
    public float shallowOcean = 0.25F;
    public float beach = 0.35F;
    public float coast = 0.4F;
    public float nearInland = 0.5F;
    public float midInland = 0.6F;
    public float farInland = 0.7F;
    
    // 地形设置
    public float terrainScaler = 1.0F;
    public int seaLevel = 63;
    public int worldHeight = 256;
    
    // 河流设置
    public int riverCount = 14;
    public float riverFrequency = 0.0025F;
    
    // 侵蚀设置
    public int erosionIterations = 12000;
    public int dropletLifetime = 32;
    
    public WorldSettings() {}
    
    public static WorldSettings defaults() {
        return new WorldSettings();
    }
}
