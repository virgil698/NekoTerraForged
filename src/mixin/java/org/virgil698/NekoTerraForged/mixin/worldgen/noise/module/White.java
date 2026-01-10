package org.virgil698.NekoTerraForged.mixin.worldgen.noise.module;

import org.virgil698.NekoTerraForged.mixin.worldgen.noise.NoiseUtil;

/**
 * 白噪声
 * 移植自 ReTerraForged
 */
public class White implements Noise {
    private final float frequency;

    public White(float frequency) {
        this.frequency = frequency;
    }

    public float frequency() {
        return frequency;
    }

    @Override
    public float compute(float x, float z, int seed) {
        x *= this.frequency;
        z *= this.frequency;
        float value = sample(x, z, seed);
        return Math.abs(value);
    }

    @Override
    public float minValue() {
        return 0.0F;
    }

    @Override
    public float maxValue() {
        return 1.0F;
    }

    @Override
    public Noise mapAll(Visitor visitor) {
        return visitor.apply(this);
    }

    public static float sample(float x, float z, int seed) {
        int xi = NoiseUtil.round(x);
        int zi = NoiseUtil.round(z);
        return NoiseUtil.valCoord2D(seed, xi, zi);
    }

    public static float sample(float x, float z, int seed, int offset) {
        return sample(x, z, NoiseUtil.hash(seed, offset));
    }
}
