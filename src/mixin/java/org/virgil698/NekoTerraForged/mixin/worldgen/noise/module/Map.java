package org.virgil698.NekoTerraForged.mixin.worldgen.noise.module;

/**
 * 映射噪声模块
 * 移植自 ReTerraForged
 */
public record Map(Noise alpha, Noise from, Noise to) implements Noise {

    @Override
    public float compute(float x, float z, int seed) {
        float alphaMin = this.alpha.minValue();
        float alphaMax = this.alpha.maxValue();

        float value = this.alpha.compute(x, z, seed);
        float alpha = (value - alphaMin) / (alphaMax - alphaMin);
        float min = this.from.compute(x, z, seed);
        float max = this.to.compute(x, z, seed);
        return min + alpha * (max - min);
    }

    @Override
    public float minValue() {
        return this.from.minValue();
    }

    @Override
    public float maxValue() {
        return this.to.maxValue();
    }

    @Override
    public Noise mapAll(Visitor visitor) {
        return visitor.apply(new Map(this.alpha.mapAll(visitor), this.from.mapAll(visitor), this.to.mapAll(visitor)));
    }
}
