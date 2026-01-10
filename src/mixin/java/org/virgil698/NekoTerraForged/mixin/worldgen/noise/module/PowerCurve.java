package org.virgil698.NekoTerraForged.mixin.worldgen.noise.module;

import org.virgil698.NekoTerraForged.mixin.worldgen.noise.NoiseUtil;

/**
 * 幂曲线噪声模块
 * 移植自 ReTerraForged
 */
public record PowerCurve(Noise input, float power, float mid, float min, float max) implements Noise {

    public PowerCurve(Noise input, float power) {
        this(input, power, input.minValue() + (input.maxValue() - input.minValue()) / 2.0F);
    }
    
    private PowerCurve(Noise input, float power, float mid) {
        this(input, power, mid, mid - NoiseUtil.pow(mid - input.minValue(), power), mid + NoiseUtil.pow(input.maxValue() - mid, power));
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
    public float compute(float x, float z, int seed) {
        float inputVal = this.input.compute(x, z, seed);
        if (inputVal >= this.mid) {
            float part = inputVal - this.mid;
            inputVal = this.mid + NoiseUtil.pow(part, this.power);
        } else {
            float part = this.mid - inputVal;
            inputVal = this.mid - NoiseUtil.pow(part, this.power);
        }
        return NoiseUtil.map(inputVal, this.min, this.max, this.max - this.min);
    }

    @Override
    public Noise mapAll(Visitor visitor) {
        return new PowerCurve(this.input.mapAll(visitor), this.power);
    }
}
