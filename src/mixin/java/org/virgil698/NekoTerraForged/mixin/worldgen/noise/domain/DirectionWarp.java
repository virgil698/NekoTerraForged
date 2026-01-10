package org.virgil698.NekoTerraForged.mixin.worldgen.noise.domain;

import org.virgil698.NekoTerraForged.mixin.worldgen.noise.NoiseUtil;
import org.virgil698.NekoTerraForged.mixin.worldgen.noise.module.Noise;
import org.virgil698.NekoTerraForged.mixin.worldgen.noise.module.Noise.Visitor;

/**
 * 方向域变形
 * 移植自 ReTerraForged
 */
public record DirectionWarp(Noise direction, Noise strength) implements Domain {

    @Override
    public float getOffsetX(float x, float z, int seed) {
        float angle = this.direction.compute(x, z, seed) * 6.2831855F;
        return NoiseUtil.sin(angle) * this.strength.compute(x, z, seed);
    }

    @Override
    public float getOffsetZ(float x, float z, int seed) {
        float angle = this.direction.compute(x, z, seed) * 6.2831855F;
        return NoiseUtil.cos(angle) * this.strength.compute(x, z, seed);
    }

    @Override
    public Domain mapAll(Visitor visitor) {
        return new DirectionWarp(this.direction.mapAll(visitor), this.strength.mapAll(visitor));
    }
}
