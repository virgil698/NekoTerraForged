package org.virgil698.NekoTerraForged.mixin.worldgen.noise.module;

import org.virgil698.NekoTerraForged.mixin.worldgen.noise.function.CurveFunction;

/**
 * 曲线噪声模块
 * 移植自 ReTerraForged
 */
public record Curve(Noise input, CurveFunction curveFunction) implements Noise {

    @Override
    public float compute(float x, float z, int seed) {
        return this.curveFunction.apply(this.input.compute(x, z, seed));
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
        return visitor.apply(new Curve(this.input.mapAll(visitor), this.curveFunction));
    }
}
