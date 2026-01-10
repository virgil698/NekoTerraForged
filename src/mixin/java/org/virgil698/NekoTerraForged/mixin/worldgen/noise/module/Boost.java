package org.virgil698.NekoTerraForged.mixin.worldgen.noise.module;

import org.virgil698.NekoTerraForged.mixin.worldgen.noise.NoiseUtil;

/**
 * 增强噪声模块
 * 移植自 ReTerraForged
 */
public record Boost(Noise input, int iterations) implements Noise {
    
    public Boost {
        input = Noises.map(input, 0.0F, 1.0F);
        iterations = Math.max(1, iterations);
    }
    
    @Override
    public float compute(float x, float z, int seed) {
        float value = this.input.compute(x, z, seed);
        for (int i = 0; i < this.iterations; ++i) {
            value = NoiseUtil.pow(value, 1.0F - value);
        }
        return value;
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
        return visitor.apply(new Boost(this.input.mapAll(visitor), this.iterations));
    }
}
