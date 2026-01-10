package org.virgil698.NekoTerraForged.mixin.worldgen.noise.module;

/**
 * 种子偏移噪声
 * 移植自 ReTerraForged
 */
public class ShiftSeed implements Noise {
    private final Noise input;
    private final int shift;

    public ShiftSeed(Noise input, int shift) {
        this.input = input;
        this.shift = shift;
    }

    public Noise input() {
        return input;
    }

    public int shift() {
        return shift;
    }

    @Override
    public float compute(float x, float z, int seed) {
        return this.input.compute(x, z, seed + this.shift);
    }

    @Override
    public Noise mapAll(Visitor visitor) {
        return visitor.apply(new ShiftSeed(this.input.mapAll(visitor), this.shift));
    }

    @Override
    public float minValue() {
        return this.input.minValue();
    }

    @Override
    public float maxValue() {
        return this.input.maxValue();
    }
}
