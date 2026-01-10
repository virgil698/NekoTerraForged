package org.virgil698.NekoTerraForged.mixin.worldgen.noise.module;

import org.virgil698.NekoTerraForged.mixin.worldgen.noise.NoiseUtil;

/**
 * 高级阶地噪声模块
 * 移植自 ReTerraForged
 */
public record AdvancedTerrace(Noise source, Noise modulation, Noise mask, Noise slope, 
                              float blendMin, float blendMax, int steps, int octaves) implements Noise {
    
    @Override
    public float compute(float x, float z, int seed) {
        float value = this.source.compute(x, z, seed);
        if (value <= this.blendMin) {
            return value;
        }
        float maskVal = this.mask.compute(x, z, seed);
        if (maskVal == 0.0F) {
            return value;
        }
        float result = value;
        float slopeVal = this.slope.compute(x, z, seed);
        float modulationVal = this.modulation.compute(x, z, seed);
        for (int i = 1; i <= this.octaves; ++i) {
            result = this.getStepped(result, this.steps * i);
            result = this.getSloped(value, result, slopeVal);
        }
        result = this.getModulated(result, modulationVal);
        float alpha = this.getAlpha(value);
        if (maskVal != 1.0F) {
            alpha *= maskVal;
        }
        return NoiseUtil.lerp(value, result, alpha);
    }

    @Override
    public float minValue() {
        return this.source.minValue();
    }

    @Override
    public float maxValue() {
        return this.source.maxValue();
    }

    @Override
    public Noise mapAll(Visitor visitor) {
        return new AdvancedTerrace(this.source.mapAll(visitor), this.modulation.mapAll(visitor), 
            this.mask.mapAll(visitor), this.slope.mapAll(visitor), this.blendMin, this.blendMax, this.steps, this.octaves);
    }
    
    private float getModulated(float value, float modulation) {
        return (value + modulation) / (this.source.maxValue() + this.modulation.maxValue());
    }
    
    private float getStepped(float value, int steps) {
        value = (float) NoiseUtil.round(value * steps);
        return value / steps;
    }
    
    private float getSloped(float value, float stepped, float slope) {
        final float delta = value - stepped;
        final float amount = delta * slope;
        return stepped + amount;
    }
    
    private float getAlpha(float value) {
        if (value > this.blendMax) {
            return 1.0F;
        }
        return (value - this.blendMin) / (this.blendMax - this.blendMin);
    }
}
