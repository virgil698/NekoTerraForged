package org.virgil698.NekoTerraForged.mixin.math;

/**
 * 数学工具类
 * 移植自 Valley
 */
public class Mth {
    public static final int PRIME_X = 501125321;
    public static final int PRIME_Y = 1136930381;
    public static final int PRIME_Z = 1720413743;
    public static final int PRIME_W = 1066037191;
    public static final int PHI = -1640531527;

    public static int Hash(int seed, int x, int y) {
        int hash = seed ^ ((x * PRIME_X) ^ (y * PRIME_Y));
        return hash * PHI;
    }

    public static double Noise(int hash) {
        return hash / 2.147483647E9;
    }

    public static int Clamp(int x, int min, int max) {
        return x <= min ? min : x >= max ? max : x;
    }

    public static float Clamp(float x, float min, float max) {
        return x <= min ? min : x >= max ? max : x;
    }

    public static double Clamp(double x, double min, double max) {
        return x <= min ? min : x >= max ? max : x;
    }

    public static double Min(double a, double b) {
        return a < b ? a : b;
    }

    public static double Max(double a, double b) {
        return a > b ? a : b;
    }

    public static int Floor(double value) {
        return value > 0.0 ? (int) value : ((int) value) - 1;
    }

    public static double Sqrt(double value) {
        return Math.sqrt(value);
    }

    public static double Lerp(double min, double max, double t) {
        return min + ((max - min) * t);
    }

    public static double ClampLerp(double min, double max, double t) {
        return min + ((max - min) * Clamp(t, 0.0, 1.0));
    }

    public static double Dist2(double ax, double ay, double bx, double by) {
        double dx = bx - ax;
        double dy = by - ay;
        return (dx * dx) + (dy * dy);
    }

    public static int Mix(int x) {
        int h = x * PHI;
        return h ^ (h >>> 16);
    }

    public static long Pack(int x, int y) {
        return ((long)x << 32) | (y & 0xFFFFFFFFL);
    }

    public static double Interp3(double t) {
        return t * t * (3.0 - (2.0 * t));
    }

    public static double Normalize(double x, double min, double max) {
        return (Clamp(x, min, max) - min) / (max - min);
    }

    public static int SizeBits(int value) {
        return 32 - Integer.numberOfLeadingZeros(value);
    }

    public static int SizePow2(int value) {
        return 1 << SizeBits(value);
    }

    public static double Pow2(double value) {
        return value * value;
    }

    public static double Pow3(double value) {
        return value * value * value;
    }

    public static double Pow4(double value) {
        double pow2 = value * value;
        return pow2 * pow2;
    }

    public static double Atan2(double y, double x) {
        return Math.atan2(y, x);
    }

    public static double Cos(double value) {
        return Math.cos(value);
    }

    public static double Sin(double value) {
        return Math.sin(value);
    }

    public static double Tan(double value) {
        return Math.tan(value);
    }

    public static boolean InBounds(int x, int y, int minX, int minY, int maxX, int maxY) {
        return x >= minX && x <= maxX && y >= minY && y <= maxY;
    }

    public static int Ceil(double value) {
        return value < 0.0 ? (int) value : ((int) value) + 1;
    }

    public static int Round(float value) {
        return (int) (value + 0.5f);
    }

    public static double LineDist(double x, double y, double ax, double ay, double bx, double by) {
        double A = x - ax;
        double B = y - ay;
        double C = bx - ax;
        double D = by - ay;
        double dot = (A * C) + (B * D);
        double d2 = (C * C) + (D * D);
        double t = d2 == 0.0 ? -1.0 : dot / d2;
        double px = ClampLerp(ax, bx, t);
        double py = ClampLerp(ay, by, t);
        double dx = x - px;
        double dy = y - py;
        return (dx * dx) + (dy * dy);
    }

    public static double LineDist(double x, double y, double ax, double ay, double ar, double bx, double by, double br) {
        double A = x - ax;
        double B = y - ay;
        double C = bx - ax;
        double D = by - ay;
        double len2 = (C * C) + (D * D);
        double t = len2 == 0.0 ? -1.0 : ((A * C) + (B * D)) / len2;
        double px = ClampLerp(ax, bx, t);
        double py = ClampLerp(ay, by, t);
        double pr = ClampLerp(ar, br, t);
        double pr2 = pr * pr;
        double dx = x - px;
        double dy = y - py;
        double d2 = (dx * dx) + (dy * dy);
        if (d2 > pr2) {
            return 1.0;
        }
        return d2 / pr2;
    }
}
