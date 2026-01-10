package org.virgil698.NekoTerraForged.mixin.worldgen.noise;

/**
 * 噪声工具类，提供各种噪声计算和数学工具方法
 * 移植自 ReTerraForged
 */
public class NoiseUtil {
    public static final int X_PRIME = 1619;
    public static final int Y_PRIME = 31337;
    public static final float CUBIC_2D_BOUNDING = 0.44444445F;
    public static final float PI2 = 6.2831855F;
    public static final float SQRT2;
    
    private static final int SIN_BITS = 12;
    private static final int SIN_MASK = ~(-1 << SIN_BITS);
    private static final int SIN_COUNT = SIN_MASK + 1;
    private static final float radFull = 6.2831855f;
    private static final float radToIndex = SIN_COUNT / radFull;
    private static final float[] SIN = new float[SIN_COUNT];

    public static final Vec2i[] MOORE;
    public static final Vec2f[] GRAD_2D;

    static {
        SQRT2 = (float) Math.sqrt(2.0);
        MOORE = new Vec2i[]{
            new Vec2i(-1, -1), new Vec2i(0, -1), new Vec2i(1, -1),
            new Vec2i(-1, 0), new Vec2i(0, 0), new Vec2i(1, 0),
            new Vec2i(-1, 1), new Vec2i(0, 1), new Vec2i(1, 1)
        };
        GRAD_2D = new Vec2f[]{
            new Vec2f(-1.0f, -1.0f), new Vec2f(1.0f, -1.0f),
            new Vec2f(-1.0f, 1.0f), new Vec2f(1.0f, 1.0f),
            new Vec2f(0.0f, -1.0f), new Vec2f(-1.0f, 0.0f),
            new Vec2f(0.0f, 1.0f), new Vec2f(1.0f, 0.0f)
        };
        
        for (int i = 0; i < SIN_COUNT; ++i) {
            SIN[i] = (float) Math.sin((i + 0.5f) / SIN_COUNT * radFull);
        }
    }

    // 映射函数
    public static float map(float value, float min, float max) {
        return map(value, min, max, max - min);
    }

    public static float map(float value, float min, float max, float range) {
        return map(value, min, max, range, true);
    }

    public static float map(float value, float min, float max, float range, boolean clamp) {
        float dif = (clamp ? clamp(value, min, max) : value) - min;
        return (dif >= range && clamp) ? 1.0F : (dif / range);
    }

    public static float map(float value, float from, float to, float min, float max) {
        float alpha = (value - min) / (max - min);
        return from + alpha * (to - from);
    }

    // 限制函数
    public static float clamp(float value, float min, float max) {
        return (value < min) ? min : (Math.min(value, max));
    }

    public static double clamp(double value, double min, double max) {
        return (value < min) ? min : (Math.min(value, max));
    }

    // 距离计算
    public static float dist2(float x1, float y1, float x2, float y2) {
        float dx = x1 - x2;
        float dy = y1 - y2;
        return dx * dx + dy * dy;
    }

    // 点积
    public static float dot(float x0, float y0, float x1, float y1) {
        return x0 * x1 + y0 * y1;
    }

    // 取整
    public static int floor(float f) {
        return (f >= 0.0F) ? ((int) f) : ((int) f - 1);
    }

    public static int round(float f) {
        return (f >= 0.0F) ? ((int) (f + 0.5F)) : ((int) (f - 0.5F));
    }

    // 插值
    public static float lerp(float a, float b, float alpha) {
        return a + alpha * (b - a);
    }

    public static double lerp(double a, double b, double alpha) {
        return a + alpha * (b - a);
    }

    public static float interpHermite(float t) {
        return t * t * (3.0F - 2.0F * t);
    }

    public static float interpQuintic(float t) {
        return t * t * t * (t * (t * 6.0F - 15.0F) + 10.0F);
    }


    // 曲线函数
    public static float curve(float t, float steepness) {
        return curve(t, 0.5F, steepness);
    }

    public static float curve(float t, float mid, float steepness) {
        return 1.0F / (1.0F + exp(-steepness * (t - mid)));
    }

    // 快速指数函数
    public static float exp(float x) {
        x = 1.0f + x / 256.0F;
        x *= x; x *= x; x *= x; x *= x;
        x *= x; x *= x; x *= x; x *= x;
        return x;
    }

    // 幂函数
    public static float pow(float value, int power) {
        if (power == 0) return 1.0F;
        if (power == 1) return value;
        if (power == 2) return value * value;
        if (power == 3) return value * value * value;
        if (power == 4) return value * value * value * value;
        float result = 1.0F;
        for (int i = 0; i < power; ++i) {
            result *= value;
        }
        return result;
    }

    public static float pow(float value, float power) {
        return (float) Math.pow(value, power);
    }

    public static float sqrt(float value) {
        return (float) Math.sqrt(value);
    }

    // 哈希函数
    public static int hash(int x, int y) {
        int hash = x;
        hash ^= 31337 * y;
        hash = hash * hash * hash * 60493;
        hash ^= hash >> 13;
        return hash;
    }

    public static int hash2D(int seed, int x, int y) {
        int hash = seed;
        hash ^= 1619 * x;
        hash ^= 31337 * y;
        hash = hash * hash * hash * 60493;
        hash ^= hash >> 13;
        return hash;
    }

    public static float valCoord2D(int seed, int x, int y) {
        int n = seed;
        n ^= 1619 * x;
        n ^= 31337 * y;
        return n * n * n * 60493 / 2.14748365E9F;
    }

    // 梯度坐标
    public static Vec2f coord2D(int seed, int x, int y) {
        int hash = seed;
        hash ^= 1619 * x;
        hash ^= 31337 * y;
        hash = hash * hash * hash * 60493;
        hash ^= hash >> 13;
        return GRAD_2D[hash & 0x7];
    }

    public static float gradCoord2D(int seed, int x, int y, float xd, float yd) {
        Vec2f g = coord2D(seed, x, y);
        return xd * g.x() + yd * g.y();
    }

    // 三角函数
    public static float sin(float r) {
        int index = (int) (r * radToIndex) & SIN_MASK;
        return SIN[index];
    }

    public static float cos(float r) {
        return sin(r + 1.5708f);
    }

    // 种子生成
    public static long seed(int x, int z) {
        return ((long) x & 0xFFFFFFFFL) | ((long) z & 0xFFFFFFFFL) << 32;
    }

    // 细胞噪声辅助
    public static Vec2f cell(int seed, int x, int y) {
        int hash = hash2D(seed, x, y);
        float vx = (hash & 0xFF) / 255.0F;
        float vy = ((hash >> 8) & 0xFF) / 255.0F;
        return new Vec2f(vx, vy);
    }

    // 线性插值（带范围）
    public static float lerp(float value, float from, float to, float min, float max) {
        float alpha = (value - min) / (max - min);
        return from + alpha * (to - from);
    }

    // 三次插值
    public static float cubicLerp(float a, float b, float c, float d, float t) {
        float p = (d - c) - (a - b);
        return t * t * t * p + t * t * ((a - b) - p) + t * (c - a) + b;
    }

    // 24方向梯度坐标
    public static float gradCoord2D_24(int seed, int x, int y, float xd, float yd) {
        int hash = hash2D(seed, x, y);
        int index = hash & 0x7;
        Vec2f g = GRAD_2D[index];
        return xd * g.x() + yd * g.y();
    }

    // 复制符号
    public static float copySign(float magnitude, float sign) {
        return Math.copySign(magnitude, sign);
    }

    public record Vec2f(float x, float y) {}
    public record Vec2i(int x, int y) {}
}
