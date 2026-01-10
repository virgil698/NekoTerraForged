package org.virgil698.NekoTerraForged.mixin.worldgen.noise.module;

import org.virgil698.NekoTerraForged.mixin.worldgen.noise.NoiseUtil;

/**
 * 反转噪声模块
 * 移植自 ReTerraForged
 */
public record Invert(Noise input) implements Noise {

    @Override
    public float compute(float x, float z, int seed) {
        float min = this.input.minValue();
        float max = this.input.maxValue();
        return max - NoiseUtil.clamp(this.input.compute(x, z, seed), min, max);
    }

    @Override
    public float minValue() {
        return this.input.minValue();
    }

    @Override
    public float maxValue() {
        return this.input.maxValue();
    }

    @Override
    public Noise mapAll(Visitor visitor) {
        return visitor.apply(new Invert(this.input.mapAll(visitor)));
    }
}
