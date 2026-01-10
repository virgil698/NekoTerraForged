package org.virgil698.NekoTerraForged.mixin.worldgen.noise.domain;

import org.virgil698.NekoTerraForged.mixin.worldgen.noise.module.Noise;

/**
 * 域变形接口，用于噪声坐标变换
 * 移植自 ReTerraForged
 */
public interface Domain {

    float getOffsetX(float x, float z, int seed);

    float getOffsetZ(float x, float z, int seed);

    Domain mapAll(Noise.Visitor visitor);

    default float getX(float x, float z, int seed) {
        return x + this.getOffsetX(x, z, seed);
    }

    default float getZ(float x, float z, int seed) {
        return z + this.getOffsetZ(x, z, seed);
    }
}
