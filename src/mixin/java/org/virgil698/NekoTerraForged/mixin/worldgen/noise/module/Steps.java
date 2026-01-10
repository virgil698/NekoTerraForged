package org.virgil698.NekoTerraForged.mixin.worldgen.noise.module;

import org.virgil698.NekoTerraForged.mixin.worldgen.noise.NoiseUtil;
import org.virgil698.NekoTerraForged.mixin.worldgen.noise.function.CurveFunction;

/**
 * 阶梯噪声模块
 * 移植自 ReTerraForged
 */
public record Steps(Noise input, Noise steps, Noise slopeMin, Noise slopeMax, CurveFunction slopeCurve) implements Noise {
    
    @Override
    public float compute(float x, float z, int seed) {
        float noiseValue = this.input.compute(x, z, seed);
        float min = this.slopeMin.compute(x, z, seed);
        float max = this.slopeMax.compute(x, z, seed);
        float stepCount = this.steps.compute(x, z, seed);
        float range = max - min;
        if (range <= 0.0F) {
            return (int) (noiseValue * stepCount) / stepCount;
        }
        noiseValue = 1.0F - noiseValue;
        float value = (int) (noiseValue * stepCount) / stepCount;
        float delta = noiseValue - value;
        float alpha = NoiseUtil.map(delta * stepCount, min, max, range);
        return 1.0F - NoiseUtil.lerp(value, noiseValue, this.slopeCurve.apply(alpha));
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
        return visitor.apply(new Steps(this.input.mapAll(visitor), this.steps.mapAll(visitor), 
            this.slopeMin.mapAll(visitor), this.slopeMax.mapAll(visitor), this.slopeCurve));
    }
}
