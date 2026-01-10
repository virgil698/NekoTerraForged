package org.virgil698.NekoTerraForged.mixin.worldgen.noise.module;

import org.virgil698.NekoTerraForged.mixin.worldgen.noise.NoiseUtil;

/**
 * 幂运算噪声模块
 * 移植自 ReTerraForged
 */
public record Power(Noise input, float power) implements Noise {

    @Override
    public float compute(float x, float z, int seed) {
        return NoiseUtil.pow(this.input.compute(x, z, seed), this.power);
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
        return visitor.apply(new Power(this.input.mapAll(visitor), this.power));
    }
}
