package org.virgil698.NekoTerraForged.mixin.worldgen.noise.module;

import org.virgil698.NekoTerraForged.mixin.worldgen.noise.NoiseUtil;

/**
 * Simplex 噪声实现
 * 移植自 ReTerraForged
 */
public class Simplex implements Noise {
    private static final float[] SIGNALS = new float[]{
        1.0F, 0.989F, 0.81F, 0.781F, 0.708F, 0.702F, 0.696F
    };

    private final float frequency;
    private final int octaves;
    private final float lacunarity;
    private final float gain;
    private final float min;
    private final float max;

    public Simplex(float frequency, int octaves, float lacunarity, float gain) {
        this.frequency = frequency;
        this.octaves = octaves;
        this.lacunarity = lacunarity;
        this.gain = gain;
        this.min = -max(octaves, gain);
        this.max = max(octaves, gain);
    }

    @Override
    public float compute(float x, float z, int seed) {
        x *= this.frequency;
        z *= this.frequency;
        float sum = 0.0F;
        float amp = 1.0F;
        for (int i = 0; i < this.octaves; ++i) {
            sum += sample(x, z, seed + i) * amp;
            x *= this.lacunarity;
            z *= this.lacunarity;
            amp *= this.gain;
        }
        return NoiseUtil.map(sum, this.min, this.max, (this.max - this.min));
    }

    @Override
    public float minValue() {
        return 0.0F;
    }

    @Override
    public float maxValue() {
        return 1.0F;
    }

    @Override
    public Noise mapAll(Visitor visitor) {
        return visitor.apply(this);
    }

    public static float sample(float x, float y, int seed) {
        return singleSimplex(x, y, seed, 79.869484F);
    }

    public static float singleSimplex(float x, float y, int seed, float scaler) {
        float t = (x + y) * 0.36602542F;
        int i = NoiseUtil.floor(x + t);
        int j = NoiseUtil.floor(y + t);
        t = (i + j) * 0.21132487F;
        float X0 = i - t;
        float Y0 = j - t;
        float x2 = x - X0;
        float y2 = y - Y0;
        int i2, j2;
        if (x2 > y2) {
            i2 = 1;
            j2 = 0;
        } else {
            i2 = 0;
            j2 = 1;
        }
        float x3 = x2 - i2 + 0.21132487F;
        float y3 = y2 - j2 + 0.21132487F;
        float x4 = x2 - 1.0F + 0.42264974F;
        float y4 = y2 - 1.0F + 0.42264974F;
        
        t = 0.5F - x2 * x2 - y2 * y2;
        float n0 = t < 0.0F ? 0.0F : t * t * t * t * NoiseUtil.gradCoord2D(seed, i, j, x2, y2);
        
        t = 0.5F - x3 * x3 - y3 * y3;
        float n2 = t < 0.0F ? 0.0F : t * t * t * t * NoiseUtil.gradCoord2D(seed, i + i2, j + j2, x3, y3);
        
        t = 0.5F - x4 * x4 - y4 * y4;
        float n3 = t < 0.0F ? 0.0F : t * t * t * t * NoiseUtil.gradCoord2D(seed, i + 1, j + 1, x4, y4);
        
        return scaler * (n0 + n2 + n3);
    }

    private static float max(int octaves, float gain) {
        float signal = signal(octaves);
        float sum = 0.0F;
        float amp = 1.0F;
        for (int i = 0; i < octaves; ++i) {
            sum += amp * signal;
            amp *= gain;
        }
        return sum;
    }

    private static float signal(int octaves) {
        int index = Math.min(octaves, SIGNALS.length - 1);
        return SIGNALS[index];
    }
}
