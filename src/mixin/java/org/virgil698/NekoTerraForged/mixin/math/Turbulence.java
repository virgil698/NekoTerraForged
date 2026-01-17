package org.virgil698.NekoTerraForged.mixin.math;

/**
 * Turbulence 噪声 - 产生湍流效果
 * 通过多个八度的域扭曲实现
 */
public class Turbulence implements Node {
    private final Node source;
    private final double power;
    private final int octaves;

    public Turbulence(Node source, double power, int octaves) {
        this.source = source;
        this.power = power;
        this.octaves = octaves;
    }

    @Override
    public double eval(int seed, double x, double y) {
        double tx = x;
        double ty = y;
        
        // 应用多层扭曲
        for (int i = 0; i < octaves; i++) {
            double frequency = Math.pow(2.0, i);
            double amplitude = power / frequency;
            
            tx += Simplex.Sample(seed + i * 2, x * frequency, y * frequency) * amplitude;
            ty += Simplex.Sample(seed + i * 2 + 1, x * frequency, y * frequency) * amplitude;
        }
        
        return source.eval(seed, tx, ty);
    }

    @Override
    public double min() {
        return source.min();
    }

    @Override
    public double max() {
        return source.max();
    }
}
