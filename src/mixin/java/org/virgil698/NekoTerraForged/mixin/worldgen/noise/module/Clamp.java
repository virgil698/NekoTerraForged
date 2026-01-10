package org.virgil698.NekoTerraForged.mixin.worldgen.noise.module;

import org.virgil698.NekoTerraForged.mixin.worldgen.noise.NoiseUtil;

/**
 * 钳制噪声模块
 * 移植自 ReTerraForged
 */
public record Clamp(Noise input, Noise min, Noise max) implements Noise {

    @Override
    public float compute(float x, float z, int seed) {
        return NoiseUtil.clamp(this.input.compute(x, z, seed), this.min.compute(x, z, seed), this.max.compute(x, z, seed));
    }

    @Override
    public float minValue() {
        return this.min.minValue();
    }

    @Override
    public float maxValue() {
        return this.max.maxValue();
    }

    @Override
    public Noise mapAll(Visitor visitor) {
        return visitor.apply(new Clamp(this.input.mapAll(visitor), this.min.mapAll(visitor), this.max.mapAll(visitor)));
    }
}
