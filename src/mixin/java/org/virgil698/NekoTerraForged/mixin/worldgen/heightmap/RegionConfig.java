package org.virgil698.NekoTerraForged.mixin.worldgen.heightmap;

import org.virgil698.NekoTerraForged.mixin.worldgen.noise.module.Noise;

/**
 * 区域配置
 * 移植自 ReTerraForged
 */
public record RegionConfig(int seed, int scale, Noise warpX, Noise warpZ, float warpStrength) {
}
