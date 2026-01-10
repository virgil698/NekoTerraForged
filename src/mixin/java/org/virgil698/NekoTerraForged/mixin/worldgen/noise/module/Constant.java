package org.virgil698.NekoTerraForged.mixin.worldgen.noise.module;

/**
 * 常量噪声（返回固定值）
 * 移植自 ReTerraForged
 */
public class Constant implements Noise {
    public static final Constant ZERO = new Constant(0.0F);
    public static final Constant ONE = new Constant(1.0F);
    public static final Constant HALF = new Constant(0.5F);

    private final float value;

    public Constant(float value) {
        this.value = value;
    }

    @Override
    public float compute(float x, float z, int seed) {
        return value;
    }

    @Override
    public float minValue() {
        return value;
    }

    @Override
    public float maxValue() {
        return value;
    }

    @Override
    public Noise mapAll(Visitor visitor) {
        return visitor.apply(this);
    }

    public float getValue() {
        return value;
    }
}
