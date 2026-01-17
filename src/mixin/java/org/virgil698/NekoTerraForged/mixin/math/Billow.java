package org.virgil698.NekoTerraForged.mixin.math;

/**
 * Billow 噪声 - 产生云状/泡沫状的噪声
 * 通过对 Simplex 噪声取绝对值实现
 */
public class Billow implements Node {
    private final int octaves;
    private final double lacunarity;
    private final double gain;
    private final double min;
    private final double max;

    public Billow(int octaves, double lacunarity, double gain) {
        this.octaves = octaves;
        this.lacunarity = lacunarity;
        this.gain = gain;
        
        // 计算范围
        double sumMin = 0.0;
        double sumMax = 0.0;
        double amplitude = 1.0;
        for (int i = 0; i < octaves; i++) {
            sumMax += amplitude;
            amplitude *= gain;
        }
        this.min = 0.0;
        this.max = sumMax;
    }

    @Override
    public double eval(int seed, double x, double y) {
        double amplitude = 1.0;
        double frequency = 1.0;
        double sum = 0.0;
        double max = 0.0;
        
        for (int i = 0; i < octaves; i++) {
            double value = Simplex.Sample(seed + i, x * frequency, y * frequency);
            sum += Math.abs(value) * amplitude;
            max += amplitude;
            amplitude *= gain;
            frequency *= lacunarity;
        }
        
        return sum / max;
    }

    @Override
    public double min() {
        return min;
    }

    @Override
    public double max() {
        return max;
    }
}
