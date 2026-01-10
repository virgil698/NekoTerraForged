package org.virgil698.NekoTerraForged.mixin.worldgen.noise.module;

/**
 * Alpha 噪声模块
 * 移植自 ReTerraForged
 */
public record Alpha(Noise input, Noise alpha) implements Noise {

    @Override
    public float compute(float x, float z, int seed) {
        float input = this.input.compute(x, z, seed);
        float alpha = this.alpha.compute(x, z, seed);
        return input * alpha + (1.0F - alpha);
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
        return visitor.apply(new Alpha(this.input.mapAll(visitor), this.alpha.mapAll(visitor)));
    }
}
