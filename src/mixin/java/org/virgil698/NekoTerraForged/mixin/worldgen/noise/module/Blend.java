package org.virgil698.NekoTerraForged.mixin.worldgen.noise.module;

import org.virgil698.NekoTerraForged.mixin.worldgen.noise.NoiseUtil;
import org.virgil698.NekoTerraForged.mixin.worldgen.noise.function.Interpolation;

/**
 * 混合噪声模块
 * 移植自 ReTerraForged
 */
public record Blend(Noise alpha, Noise lower, Noise upper, float mid, float range, Interpolation interpolation) implements Noise {

    @Override
    public float minValue() {
        return Math.min(this.lower.minValue(), this.upper.minValue());
    }

    @Override
    public float maxValue() {
        return Math.max(this.lower.maxValue(), this.upper.maxValue());
    }

    @Override
    public float compute(float x, float z, int seed) {
        float mid = this.alpha.minValue() + (this.alpha.maxValue() - this.alpha.minValue()) * this.mid;
        float blendLower = Math.max(this.alpha.minValue(), mid - this.range / 2.0F);
        float blendUpper = Math.min(this.alpha.maxValue(), mid + this.range / 2.0F);
        float blendRange = blendUpper - blendLower;
        float alpha = this.alpha.compute(x, z, seed);
        if (alpha < blendLower) {
            return this.lower.compute(x, z, seed);
        }
        if (alpha > blendUpper) {
            return this.upper.compute(x, z, seed);
        }
        return NoiseUtil.lerp(this.lower.compute(x, z, seed), this.upper.compute(x, z, seed), 
                this.interpolation.apply((alpha - blendLower) / blendRange));
    }

    @Override
    public Noise mapAll(Visitor visitor) {
        return visitor.apply(new Blend(this.alpha.mapAll(visitor), this.lower.mapAll(visitor), 
                this.upper.mapAll(visitor), this.mid, this.range, this.interpolation));
    }
}
