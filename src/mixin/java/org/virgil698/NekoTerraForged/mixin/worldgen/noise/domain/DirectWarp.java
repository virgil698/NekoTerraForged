package org.virgil698.NekoTerraForged.mixin.worldgen.noise.domain;

import org.virgil698.NekoTerraForged.mixin.worldgen.noise.module.Noise.Visitor;

/**
 * 直接域变形（无变形）
 * 移植自 ReTerraForged
 */
public record DirectWarp() implements Domain {

    @Override
    public float getOffsetX(float x, float z, int seed) {
        return 0.0F;
    }

    @Override
    public float getOffsetZ(float x, float z, int seed) {
        return 0.0F;
    }

    @Override
    public Domain mapAll(Visitor visitor) {
        return this;
    }
}
