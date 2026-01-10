package org.virgil698.NekoTerraForged.mixin.worldgen.noise.module;

import org.virgil698.NekoTerraForged.mixin.worldgen.noise.NoiseUtil;
import org.virgil698.NekoTerraForged.mixin.worldgen.noise.function.Interpolation;

/**
 * Simplex 山脉噪声
 * 移植自 ReTerraForged
 */
public class SimplexRidge implements Noise {
    private final float frequency;
    private final int octaves;
    private final float lacunarity;
    private final float gain;
    private final Interpolation interpolation;
    private final float[] spectralWeights;
    private final float min;
    private final float max;

    public SimplexRidge(float frequency, int octaves, float lacunarity, float gain, Interpolation interpolation) {
        this(frequency, octaves, lacunarity, gain, interpolation, calculateSpectralWeights(lacunarity));
    }

    public SimplexRidge(float frequency, int octaves, float lacunarity, float gain) {
        this(frequency, octaves, lacunarity, gain, Interpolation.CURVE3);
    }

    private SimplexRidge(float frequency, int octaves, float lacunarity, float gain, Interpolation interpolation, float[] spectralWeights) {
        this(frequency, octaves, lacunarity, gain, interpolation, spectralWeights, 0.0F, Simplex2.max(octaves, gain));
    }

    public SimplexRidge(float frequency, int octaves, float lacunarity, float gain, Interpolation interpolation, float[] spectralWeights, float min, float max) {
        this.frequency = frequency;
        this.octaves = octaves;
        this.lacunarity = lacunarity;
        this.gain = gain;
        this.interpolation = interpolation;
        this.spectralWeights = spectralWeights;
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
        float value = 0.0F;
        float weight = 1.0F;
        float offset = 1.0F;
        float amp = 2.0F;
        for (int octave = 0; octave < this.octaves; ++octave) {
            float signal = Simplex2.sample(x, z, seed + octave);
            signal = Math.abs(signal);
            signal = offset - signal;
            signal *= signal;
            signal *= weight;
            weight = signal * amp;
            weight = NoiseUtil.clamp(weight, 0.0F, 1.0F);
            value += signal * this.spectralWeights[octave];
            x *= this.lacunarity;
            z *= this.lacunarity;
            amp *= this.gain;
        }
        return NoiseUtil.map(value, this.min, this.max, Math.abs(this.max - this.min));
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

    private static float[] calculateSpectralWeights(float lacunarity) {
        float frequency = 1.0F;
        float[] spectralWeights = new float[30];
        for (int i = 0; i < spectralWeights.length; i++) {
            spectralWeights[i] = NoiseUtil.pow(frequency, -1.0F);
            frequency *= lacunarity;
        }
        return spectralWeights;
    }
}
