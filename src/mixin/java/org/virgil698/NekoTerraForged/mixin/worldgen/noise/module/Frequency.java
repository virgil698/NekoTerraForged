package org.virgil698.NekoTerraForged.mixin.worldgen.noise.module;

/**
 * 频率噪声模块
 * 移植自 ReTerraForged
 */
public record Frequency(Noise input, Noise xFreq, Noise zFreq) implements Noise {

    @Override
    public float compute(float x, float z, int seed) {
        float xFreq = this.xFreq.compute(x, z, seed);
        float zFreq = this.zFreq.compute(x, z, seed);
        return this.input.compute(x * xFreq, z * zFreq, seed);
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
        return visitor.apply(new Frequency(this.input.mapAll(visitor), this.xFreq.mapAll(visitor), this.zFreq.mapAll(visitor)));
    }
}
