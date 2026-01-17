package org.virgil698.NekoTerraForged.mixin.math;

/**
 * Ridge 噪声 - 产生山脊状的噪声
 * 通过 1 - |noise| 实现
 */
public class Ridge implements Node {
    private final int octaves;
    private final double lacunarity;
    private final double gain;

    public Ridge(int octaves, double lacunarity, double gain) {
        this.octaves = octaves;
        this.lacunarity = lacunarity;
        this.gain = gain;
    }

    @Override
    public double eval(int seed, double x, double y) {
        double amplitude = 1.0;
        double frequency = 1.0;
        double sum = 0.0;
        double max = 0.0;
        
        for (int i = 0; i < octaves; i++) {
            double value = Simplex.Sample(seed + i, x * frequency, y * frequency);
            double ridge = 1.0 - Math.abs(value);
            sum += ridge * amplitude;
            max += amplitude;
            amplitude *= gain;
            frequency *= lacunarity;
        }
        
        return (sum / max) * 2.0 - 1.0; // 重新映射到 -1 到 1
    }

    @Override
    public double min() {
        return -1.0;
    }

    @Override
    public double max() {
        return 1.0;
    }
}
