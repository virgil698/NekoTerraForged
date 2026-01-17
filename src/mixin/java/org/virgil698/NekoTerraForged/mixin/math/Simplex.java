package org.virgil698.NekoTerraForged.mixin.math;

/**
 * Simplex 噪声实现
 * 移植自 Valley
 */
public class Simplex implements Node {
    public static final Simplex INSTANCE = new Simplex();
    
    private static final double F2 = 0.366025403784439;
    private static final double G2 = 0.211324865405187;
    
    private static final double[] GRAD_2D = {
        0.13052618503570557, 0.9914448857307434, 0.3826834261417389, 0.9238795042037964,
        0.6087614297866821, 0.7933533191680908, 0.6087614297866821, 0.7933533191680908,
        0.7933533191680908, 0.6087614297866821, 0.9238795042037964, 0.3826834261417389,
        0.9914448857307434, 0.13052618503570557, 0.9914448857307434, 0.13052618503570557,
        0.9914448857307434, -0.13052618503570557, 0.9238795042037964, -0.3826834261417389,
        0.7933533191680908, -0.6087614297866821, 0.7933533191680908, -0.6087614297866821,
        0.6087614297866821, -0.7933533191680908, 0.3826834261417389, -0.9238795042037964,
        0.13052618503570557, -0.9914448857307434, 0.13052618503570557, -0.9914448857307434,
        -0.13052618503570557, -0.9914448857307434, -0.3826834261417389, -0.9238795042037964,
        -0.6087614297866821, -0.7933533191680908, -0.6087614297866821, -0.7933533191680908,
        -0.7933533191680908, -0.6087614297866821, -0.9238795042037964, -0.3826834261417389,
        -0.9914448857307434, -0.13052618503570557, -0.9914448857307434, -0.13052618503570557,
        -0.9914448857307434, 0.13052618503570557, -0.9238795042037964, 0.3826834261417389,
        -0.7933533191680908, 0.6087614297866821, -0.7933533191680908, 0.6087614297866821,
        -0.6087614297866821, 0.7933533191680908, -0.3826834261417389, 0.9238795042037964,
        -0.13052618503570557, 0.9914448857307434, -0.13052618503570557, 0.9914448857307434
    };

    @Override
    public double eval(int seed, double x, double y) {
        return Sample(seed, x, y);
    }

    @Override
    public double min() {
        return -1.0;
    }

    @Override
    public double max() {
        return 1.0;
    }

    public static double Sample(int seed, double x, double y) {
        double t = (x + y) * F2;
        int i = Mth.Floor(x + t);
        int j = Mth.Floor(y + t);
        
        double g = (i + j) * G2;
        double X0 = i - g;
        double Y0 = j - g;
        double x0 = x - X0;
        double y0 = y - Y0;
        
        int i1 = x0 > y0 ? 1 : 0;
        int j1 = x0 > y0 ? 0 : 1;
        
        double x1 = (x0 - i1) + G2;
        double y1 = (y0 - j1) + G2;
        double x2 = (x0 - 1.0) + 0.422649730810374;
        double y2 = (y0 - 1.0) + 0.422649730810374;
        
        double t0 = (0.5 - (x0 * x0)) - (y0 * y0);
        double t1 = (0.5 - (x1 * x1)) - (y1 * y1);
        double t2 = (0.5 - (x2 * x2)) - (y2 * y2);
        
        double n0 = t0 < 0.0 ? 0.0 : GradCoord2D(seed, i, j, x0, y0, t0);
        double n1 = t1 < 0.0 ? 0.0 : GradCoord2D(seed, i + i1, j + j1, x1, y1, t1);
        double n2 = t2 < 0.0 ? 0.0 : GradCoord2D(seed, i + 1, j + 1, x2, y2, t2);
        
        return 99.83685446303647 * (n0 + n1 + n2);
    }

    private static double GradCoord2D(int seed, int x, int y, double xd, double yd, double t) {
        int hash = seed ^ ((Mth.PRIME_X * x) ^ (Mth.PRIME_Y * y));
        int hash2 = hash * hash * hash * 60493;
        int index = (((int) ((((hash2 >> 13) ^ hash2) & 4194303) * 1.3333334f)) & 31) << 1;
        double t2 = t * t;
        return ((xd * GRAD_2D[index]) + (yd * GRAD_2D[index + 1])) * t2 * t2;
    }
}
