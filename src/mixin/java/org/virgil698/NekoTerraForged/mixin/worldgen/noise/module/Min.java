package org.virgil698.NekoTerraForged.mixin.worldgen.noise.module;

/**
 * 最小值噪声模块
 * 移植自 ReTerraForged
 */
public record Min(Noise input1, Noise input2) implements Noise {

    @Override
    public float compute(float x, float z, int seed) {
        return Math.min(this.input1.compute(x, z, seed), this.input2.compute(x, z, seed));
    }

    @Override
    public float minValue() {
        return Math.min(this.input1.minValue(), this.input2.minValue());
    }

    @Override
    public float maxValue() {
        return Math.min(this.input1.maxValue(), this.input2.maxValue());
    }

    @Override
    public Noise mapAll(Visitor visitor) {
        return new Min(this.input1.mapAll(visitor), this.input2.mapAll(visitor));
    }
}
