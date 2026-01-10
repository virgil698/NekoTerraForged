package org.virgil698.NekoTerraForged.mixin.worldgen.noise.module;

import org.virgil698.NekoTerraForged.mixin.worldgen.noise.NoiseUtil;
import org.virgil698.NekoTerraForged.mixin.worldgen.noise.function.Interpolation;

/**
 * Simplex2 噪声
 * 移植自 ReTerraForged
 */
public class Simplex2 implements Noise {
    private static final float[] SIGNALS = new float[] {
        1.0F, 0.989F, 0.81F, 0.781F, 0.708F, 0.702F, 0.696F
    };

    private final float frequency;
    private final int octaves;
    private final float lacunarity;
    private final float gain;
    private final Interpolation interpolation;
    private final float min;
    private final float max;

    public Simplex2(float frequency, int octaves, float lacunarity, float gain, Interpolation interpolation) {
        this(frequency, octaves, lacunarity, gain, interpolation, -max(octaves, gain), max(octaves, gain));
    }

    public Simplex2(float frequency, int octaves, float lacunarity, float gain) {
        this(frequency, octaves, lacunarity, gain, Interpolation.CURVE3);
    }

    public Simplex2(float frequency, int octaves, float lacunarity, float gain, Interpolation interpolation, float min, float max) {
        this.frequency = frequency;
        this.octaves = Math.min(octaves, 30);
        this.lacunarity = lacunarity;
        this.gain = gain;
        this.interpolation = interpolation;
        this.min = min;
        this.max = max;
    }

    public float frequency() { return frequency; }
    public int octaves() { return octaves; }
    public float lacunarity() { return lacunarity; }
    public float gain() { return gain; }
    public Interpolation interpolation() { return interpolation; }

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
        return Simplex.singleSimplex(x, y, seed, 99.83685F);
    }

    public static float max(int octaves, float gain) {
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
