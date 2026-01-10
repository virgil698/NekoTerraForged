package org.virgil698.NekoTerraForged.mixin.worldgen.noise.module;

import org.virgil698.NekoTerraForged.mixin.worldgen.noise.NoiseUtil;
import org.virgil698.NekoTerraForged.mixin.worldgen.noise.function.Interpolation;

/**
 * Billow 噪声（湍流噪声）
 * 移植自 ReTerraForged
 */
public class Billow implements Noise {
    private final float frequency;
    private final int octaves;
    private final float lacunarity;
    private final float gain;
    private final Interpolation interpolation;
    private final float[] spectralWeights;
    private final float min;
    private final float max;

    public Billow(float frequency, int octaves, float lacunarity, float gain, Interpolation interpolation) {
        this(frequency, octaves, lacunarity, gain, interpolation, calculateSpectralWeights(octaves, lacunarity));
    }

    private Billow(float frequency, int octaves, float lacunarity, float gain, Interpolation interpolation, float[] spectralWeights) {
        this(frequency, Math.min(octaves, 30), lacunarity, gain, interpolation, spectralWeights, 0.0F, calculateMaxBound(spectralWeights, gain));
    }

    public Billow(float frequency, int octaves, float lacunarity, float gain, Interpolation interpolation, float[] spectralWeights, float min, float max) {
        this.frequency = frequency;
        this.octaves = Math.min(octaves, 30);
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
        float amp = 2.0F;
        float value = 0.0F;
        float weight = 1.0F;
        for (int octave = 0; octave < this.octaves; ++octave) {
            float signal = Perlin.sample(x, z, seed + octave, this.interpolation);
            signal = 1.0F - Math.abs(signal);
            signal *= signal;
            signal *= weight;
            weight = signal * amp;
            weight = NoiseUtil.clamp(weight, 0.0F, 1.0F);
            value += signal * this.spectralWeights[octave];
            x *= this.lacunarity;
            z *= this.lacunarity;
            amp *= this.gain;
        }
        return 1.0F - NoiseUtil.map(value, this.min, this.max, Math.abs(this.max - this.min));
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

    private static float[] calculateSpectralWeights(int octaves, float lacunarity) {
        float frequency = 1.0F;
        float[] spectralWeights = new float[Math.min(octaves, 30)];
        for (int i = 0; i < spectralWeights.length; i++) {
            spectralWeights[i] = NoiseUtil.pow(frequency, -1.0F);
            frequency *= lacunarity;
        }
        return spectralWeights;
    }

    private static float calculateMaxBound(float[] spectralWeights, float gain) {
        float amp = 2.0F;
        float value = 0.0F;
        float weight = 1.0F;
        for (int curOctave = 0; curOctave < spectralWeights.length; ++curOctave) {
            float noise = 1.0F;
            noise *= weight;
            weight = noise * amp;
            weight = Math.min(1.0F, Math.max(0.0F, weight));
            value += noise * spectralWeights[curOctave];
            amp *= gain;
        }
        return value;
    }
}
