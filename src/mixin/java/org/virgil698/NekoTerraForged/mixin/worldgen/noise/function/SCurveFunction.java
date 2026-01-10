package org.virgil698.NekoTerraForged.mixin.worldgen.noise.function;

import org.virgil698.NekoTerraForged.mixin.worldgen.noise.NoiseUtil;

/**
 * S曲线函数
 * 移植自 ReTerraForged
 */
public record SCurveFunction(float lower, float upper) implements CurveFunction {

    public SCurveFunction {
        upper = upper < 0.0F ? Math.max(-lower, upper) : upper;
    }

    @Override
    public float apply(float f) {
        return NoiseUtil.pow(f, this.lower + this.upper * f);
    }
}
