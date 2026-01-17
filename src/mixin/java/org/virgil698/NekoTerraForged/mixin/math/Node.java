package org.virgil698.NekoTerraForged.mixin.math;

/**
 * 噪声节点接口
 * 移植自 Valley
 */
public interface Node {
    double eval(int seed, double x, double y);

    default double min() {
        return -1.0;
    }

    default double max() {
        return 1.0;
    }

    static Simplex Simplex() {
        return Simplex.INSTANCE;
    }

    static White White() {
        return White.INSTANCE;
    }

    static Cell Cell(Cell.GridType grid, Cell.ResultType result, double jitter) {
        return new Cell(grid, result, jitter);
    }

    static FBM Fbm(Node source, int octaves, double lacunarity, double gain) {
        double sumMin = 0.0;
        double sumMax = 0.0;
        double amplitude = 1.0;
        for (int i = 0; i < octaves; i++) {
            sumMin += source.min() * amplitude;
            sumMax += source.max() * amplitude;
            amplitude *= gain;
        }
        return new FBM(source, octaves, lacunarity, gain, sumMin, sumMax);
    }

    static Remap Remap(Node source, Spline spline) {
        return new Remap(source, spline);
    }

    static Scale Scale(Node source, int scale) {
        return new Scale(source, scale, 1.0 / Mth.Max(scale, 1.0));
    }

    static Warp Warp(Node source, Node warp, double frequency, double amplitude) {
        return new Warp(source, warp, frequency, amplitude);
    }

    // White 噪声
    class White implements Node {
        public static final White INSTANCE = new White();

        private White() {}

        @Override
        public double eval(int seed, double x, double y) {
            return Mth.Noise(Mth.Hash(seed, Mth.Floor(x), Mth.Floor(y)));
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

    // FBM (Fractional Brownian Motion)
    class FBM implements Node {
        private final Node source;
        private final int octaves;
        private final double lacunarity;
        private final double gain;
        private final double min;
        private final double max;

        public FBM(Node source, int octaves, double lacunarity, double gain, double min, double max) {
            this.source = source;
            this.octaves = octaves;
            this.lacunarity = lacunarity;
            this.gain = gain;
            this.min = min;
            this.max = max;
        }

        @Override
        public double eval(int seed, double x, double y) {
            double amplitude = 1.0;
            double frequency = 1.0;
            double sum = 0.0;
            double max = 0.0;
            for (int i = 0; i < octaves; i++) {
                sum += source.eval(seed, x * frequency, y * frequency) * amplitude;
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

    // Remap using spline
    class Remap implements Node {
        private final Node source;
        private final Spline spline;

        public Remap(Node source, Spline spline) {
            this.source = source;
            this.spline = spline;
        }

        @Override
        public double eval(int seed, double x, double y) {
            return spline.eval(source.eval(seed, x, y));
        }
    }

    // Scale
    class Scale implements Node {
        private final Node source;
        private final int scale;
        private final double frequency;

        public Scale(Node source, int scale, double frequency) {
            this.source = source;
            this.scale = scale;
            this.frequency = frequency;
        }

        @Override
        public double eval(int seed, double x, double y) {
            return source.eval(seed, x * frequency, y * frequency);
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

    // Warp (domain warping)
    class Warp implements Node {
        private final Node source;
        private final Node warp;
        private final double frequency;
        private final double amplitude;
        private static final int X_OFFSET = 1066037191;
        private static final int Y_OFFSET = 1720413743;

        public Warp(Node source, Node warp, double frequency, double amplitude) {
            this.source = source;
            this.warp = warp;
            this.frequency = frequency;
            this.amplitude = amplitude;
        }

        @Override
        public double eval(int seed, double x, double y) {
            double sx = x * frequency;
            double sy = y * frequency;
            double wx = warp.eval(seed + X_OFFSET, sx, sy) * amplitude;
            double wy = warp.eval(seed + Y_OFFSET, sx, sy) * amplitude;
            return source.eval(seed, x + wx, y + wy);
        }

        public double pointX(int seed, double x, double y) {
            return x + warp.eval(seed + X_OFFSET, x * frequency, y * frequency) * amplitude;
        }

        public double pointY(int seed, double x, double y) {
            return y + warp.eval(seed + Y_OFFSET, x * frequency, y * frequency) * amplitude;
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
}
