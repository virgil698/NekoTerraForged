package org.virgil698.NekoTerraForged.mixin.worldgen.noise.module;

/**
 * 绝对值噪声模块
 * 移植自 ReTerraForged
 */
public record Abs(Noise input) implements Noise {

    @Override
    public float compute(float x, float z, int seed) {
        return Math.abs(this.input.compute(x, z, seed));
    }

    @Override
    public float minValue() {
        return Math.abs(this.input.minValue());
    }

    @Override
    public float maxValue() {
        return Math.abs(this.input.maxValue());
    }

    @Override
    public Noise mapAll(Visitor visitor) {
        return visitor.apply(new Abs(this.input.mapAll(visitor)));
    }
}
