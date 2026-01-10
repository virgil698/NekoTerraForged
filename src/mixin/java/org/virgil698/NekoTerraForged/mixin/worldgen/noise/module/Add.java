package org.virgil698.NekoTerraForged.mixin.worldgen.noise.module;

/**
 * 加法噪声模块
 * 移植自 ReTerraForged
 */
public record Add(Noise input1, Noise input2) implements Noise {

    @Override
    public float compute(float x, float z, int seed) {
        return this.input1.compute(x, z, seed) + this.input2.compute(x, z, seed);
    }

    @Override
    public float minValue() {
        return this.input1.minValue() + this.input2.minValue();
    }

    @Override
    public float maxValue() {
        return this.input1.maxValue() + this.input2.maxValue();
    }

    @Override
    public Noise mapAll(Visitor visitor) {
        return visitor.apply(new Add(this.input1.mapAll(visitor), this.input2.mapAll(visitor)));
    }
}
