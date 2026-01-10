package org.virgil698.NekoTerraForged.mixin.worldgen.noise.module;

import org.virgil698.NekoTerraForged.mixin.worldgen.cell.CellField;
import org.virgil698.NekoTerraForged.mixin.worldgen.noise.domain.Domain;
import org.virgil698.NekoTerraForged.mixin.worldgen.noise.domain.Domains;
import org.virgil698.NekoTerraForged.mixin.worldgen.noise.function.CellFunction;
import org.virgil698.NekoTerraForged.mixin.worldgen.noise.function.DistanceFunction;
import org.virgil698.NekoTerraForged.mixin.worldgen.noise.function.EdgeFunction;
import org.virgil698.NekoTerraForged.mixin.worldgen.noise.function.Interpolation;

/**
 * 噪声工厂类，提供创建各种噪声的便捷方法
 * 移植自 ReTerraForged
 */
public final class Noises {
    private Noises() {}

    // ==================== 常量噪声 ====================
    public static Noise constant(float value) {
        return new Constant(value);
    }

    public static Noise zero() {
        return Constant.ZERO;
    }

    public static Noise one() {
        return Constant.ONE;
    }

    public static Noise negative(Noise input) {
        return mul(input, -1.0F);
    }

    // ==================== White 噪声 ====================
    public static Noise white(int seed, int scale) {
        return shiftSeed(new White(1.0F / scale), seed);
    }

    // ==================== Perlin 噪声 ====================
    public static Noise perlin(int seed, int scale, int octaves) {
        return perlin(seed, scale, octaves, 2.0F);
    }

    public static Noise perlin(int seed, int scale, int octaves, float lacunarity) {
        return perlin(seed, scale, octaves, lacunarity, 0.5F);
    }

    public static Noise perlin(int seed, int scale, int octaves, float lacunarity, float gain) {
        return new Perlin(seed, 1.0F / scale, octaves, lacunarity, gain, Interpolation.CURVE3);
    }

    public static Noise perlin2(int seed, int scale, int octaves) {
        return perlin2(seed, scale, octaves, 2.0F);
    }

    public static Noise perlin2(int seed, int scale, int octaves, float lacunarity) {
        return perlin2(seed, scale, octaves, lacunarity, 0.5F);
    }

    public static Noise perlin2(int seed, int scale, int octaves, float lacunarity, float gain) {
        float frequency = 1.0F / scale;
        return new Perlin(seed, frequency, octaves, lacunarity, gain, Interpolation.CURVE4);
    }

    // ==================== Simplex 噪声 ====================
    public static Noise simplex(int seed, int scale, int octaves) {
        return simplex(seed, scale, octaves, 2.0F);
    }

    public static Noise simplex(int seed, int scale, int octaves, float lacunarity) {
        return simplex(seed, scale, octaves, lacunarity, 0.5F);
    }

    public static Noise simplex(int seed, int scale, int octaves, float lacunarity, float gain) {
        return shiftSeed(new Simplex(1.0F / scale, octaves, lacunarity, gain), seed);
    }

    public static Noise simplex2(int seed, int scale, int octaves) {
        return simplex2(seed, scale, octaves, 2.0F);
    }

    public static Noise simplex2(int seed, int scale, int octaves, float lacunarity) {
        return simplex2(seed, scale, octaves, lacunarity, 0.5F);
    }

    public static Noise simplex2(int seed, int scale, int octaves, float lacunarity, float gain) {
        return shiftSeed(new Simplex2(1.0F / scale, octaves, lacunarity, gain), seed);
    }

    public static Noise simplexRidge(int seed, int scale, int octaves) {
        return simplexRidge(seed, scale, octaves, 2.0F);
    }

    public static Noise simplexRidge(int seed, int scale, int octaves, float lacunarity) {
        return simplexRidge(seed, scale, octaves, lacunarity, 0.975F);
    }

    public static Noise simplexRidge(int seed, int scale, int octaves, float lacunarity, float gain) {
        return shiftSeed(new SimplexRidge(1.0F / scale, octaves, lacunarity, gain), seed);
    }

    // ==================== Worley 噪声 ====================
    public static Noise worley(int seed, int scale) {
        return worley(seed, scale, CellFunction.CELL_VALUE, DistanceFunction.EUCLIDEAN, zero());
    }

    public static Noise worley(int seed, int scale, CellFunction cellFunction, DistanceFunction distanceFunction, Noise lookup) {
        return shiftSeed(new Worley(1.0F / scale, 1.0F, cellFunction, distanceFunction, lookup), seed);
    }

    public static Noise worleyEdge(int seed, int scale) {
        return worleyEdge(seed, scale, EdgeFunction.DISTANCE_2, DistanceFunction.EUCLIDEAN);
    }

    public static Noise worleyEdge(int seed, int scale, EdgeFunction edgeFunction, DistanceFunction distanceFunction) {
        return shiftSeed(new WorleyEdge(1.0F / scale, 1.0F, edgeFunction, distanceFunction), seed);
    }

    // ==================== Billow 噪声 ====================
    public static Noise billow(int seed, int scale, int octaves) {
        return billow(seed, scale, octaves, 2.0F);
    }

    public static Noise billow(int seed, int scale, int octaves, float lacunarity) {
        return billow(seed, scale, octaves, lacunarity, 0.5F);
    }

    public static Noise billow(int seed, int scale, int octaves, float lacunarity, float gain) {
        return shiftSeed(new Billow(1.0F / scale, octaves, lacunarity, gain, Interpolation.CURVE3), seed);
    }

    // ==================== 种子偏移 ====================
    public static Noise shiftSeed(Noise input, int seed) {
        return new ShiftSeed(input, seed);
    }

    // ==================== 噪声操作 ====================
    public static Noise add(Noise a, Noise b) {
        return new Add(a, b);
    }

    public static Noise add(Noise noise, float value) {
        return add(noise, constant(value));
    }

    public static Noise mul(Noise a, Noise b) {
        return new Multiply(a, b);
    }

    public static Noise mul(Noise noise, float value) {
        return mul(noise, constant(value));
    }

    public static Noise clamp(Noise noise, float min, float max) {
        return new ClampNoise(noise, min, max);
    }

    public static Noise map(Noise noise, float min, float max) {
        return new MapNoise(noise, min, max);
    }

    public static Noise min(Noise a, Noise b) {
        return new Min(a, b);
    }

    public static Noise min(Noise noise, float value) {
        return min(noise, constant(value));
    }

    public static Noise max(Noise a, Noise b) {
        return new Max(a, b);
    }

    public static Noise max(Noise noise, float value) {
        return max(noise, constant(value));
    }

    public static Noise abs(Noise noise) {
        return new Abs(noise);
    }

    public static Noise invert(Noise noise) {
        return new Invert(noise);
    }

    public static Noise pow(Noise noise, float power) {
        return new Power(noise, power);
    }

    public static Noise curve(Noise noise, org.virgil698.NekoTerraForged.mixin.worldgen.noise.function.CurveFunction curveFunction) {
        return new Curve(noise, curveFunction);
    }

    // ==================== Warp 噪声 ====================
    public static Noise warpPerlin(Noise input, int seed, int scale, int octaves, float pow) {
        return warp(input,
            perlin(seed, scale, octaves),
            perlin(seed + 1, scale, octaves),
            pow
        );
    }

    public static Noise warpWhite(Noise input, int seed, int scale, float pow) {
        return warp(input,
            white(seed, scale),
            white(seed + 1, scale),
            pow
        );
    }

    public static Noise warp(Noise input, Noise warpX, Noise warpZ, float pow) {
        return warp(input, warpX, warpZ, constant(pow));
    }

    public static Noise warp(Noise input, Noise warpX, Noise warpZ, Noise pow) {
        return warp(input, Domains.domain(warpX, warpZ, pow));
    }

    public static Noise warp(Noise input, Domain domain) {
        return new Warp(input, domain);
    }

    // ==================== 缓存 ====================
    public static Noise cache2d(Noise input) {
        return new Cache2d(input);
    }


    // ==================== 内部噪声类 ====================
    private static class ClampNoise implements Noise {
        private final Noise source;
        private final float min, max;

        ClampNoise(Noise source, float min, float max) {
            this.source = source;
            this.min = min;
            this.max = max;
        }

        @Override
        public float compute(float x, float z, int seed) {
            float value = source.compute(x, z, seed);
            return Math.max(min, Math.min(max, value));
        }

        @Override
        public float minValue() {
            return min;
        }

        @Override
        public float maxValue() {
            return max;
        }

        @Override
        public Noise mapAll(Visitor visitor) {
            return visitor.apply(new ClampNoise(source.mapAll(visitor), min, max));
        }
    }

    private static class MapNoise implements Noise {
        private final Noise source;
        private final float min, max;

        MapNoise(Noise source, float min, float max) {
            this.source = source;
            this.min = min;
            this.max = max;
        }

        @Override
        public float compute(float x, float z, int seed) {
            float value = source.compute(x, z, seed);
            float srcMin = source.minValue();
            float srcMax = source.maxValue();
            float alpha = (value - srcMin) / (srcMax - srcMin);
            return min + alpha * (max - min);
        }

        @Override
        public float minValue() {
            return min;
        }

        @Override
        public float maxValue() {
            return max;
        }

        @Override
        public Noise mapAll(Visitor visitor) {
            return visitor.apply(new MapNoise(source.mapAll(visitor), min, max));
        }
    }

    // ==================== Frequency 噪声 ====================
    public static Noise frequency(Noise noise, float frequency) {
        return new FrequencyNoise(noise, frequency);
    }

    private static class FrequencyNoise implements Noise {
        private final Noise source;
        private final float frequency;

        FrequencyNoise(Noise source, float frequency) {
            this.source = source;
            this.frequency = frequency;
        }

        @Override
        public float compute(float x, float z, int seed) {
            return source.compute(x * frequency, z * frequency, seed);
        }

        @Override
        public float minValue() {
            return source.minValue();
        }

        @Override
        public float maxValue() {
            return source.maxValue();
        }

        @Override
        public Noise mapAll(Visitor visitor) {
            return visitor.apply(new FrequencyNoise(source.mapAll(visitor), frequency));
        }
    }

    // ==================== Alpha 噪声 ====================
    public static Noise alpha(Noise noise, float alpha) {
        return add(mul(noise, alpha), constant(1.0F - alpha));
    }

    // ==================== Blend 噪声 ====================
    public static Noise blend(Noise selector, Noise lower, Noise upper, float blendLower, float blendUpper) {
        return new BlendNoise(selector, lower, upper, blendLower, blendUpper);
    }

    // ==================== Threshold 噪声 ====================
    public static Noise threshold(Noise selector, Noise lower, Noise upper, float threshold) {
        return new ThresholdNoise(selector, lower, upper, threshold);
    }

    private static class ThresholdNoise implements Noise {
        private final Noise selector;
        private final Noise lower;
        private final Noise upper;
        private final float threshold;

        ThresholdNoise(Noise selector, Noise lower, Noise upper, float threshold) {
            this.selector = selector;
            this.lower = lower;
            this.upper = upper;
            this.threshold = threshold;
        }

        @Override
        public float compute(float x, float z, int seed) {
            float select = selector.compute(x, z, seed);
            if (select < threshold) {
                return lower.compute(x, z, seed);
            }
            return upper.compute(x, z, seed);
        }

        @Override
        public float minValue() {
            return Math.min(lower.minValue(), upper.minValue());
        }

        @Override
        public float maxValue() {
            return Math.max(lower.maxValue(), upper.maxValue());
        }

        @Override
        public Noise mapAll(Visitor visitor) {
            return visitor.apply(new ThresholdNoise(
                selector.mapAll(visitor),
                lower.mapAll(visitor),
                upper.mapAll(visitor),
                threshold
            ));
        }
    }

    // ==================== Cell 噪声 (用于从 Cell 读取字段) ====================
    public static Noise cell(CellField field) {
        return new CellNoise(field);
    }

    /**
     * Cell 噪声 - 用于从 Cell 中读取字段值
     * 这是一个标记类，实际值由 CellSampler.Provider 在运行时提供
     */
    public static class CellNoise implements MappedNoise.Marker {
        private final CellField field;

        public CellNoise(CellField field) {
            this.field = field;
        }

        public CellField field() {
            return field;
        }

        @Override
        public Noise mapAll(Visitor visitor) {
            return visitor.apply(this);
        }
    }

    private static class BlendNoise implements Noise {
        private final Noise selector;
        private final Noise lower;
        private final Noise upper;
        private final float blendLower;
        private final float blendUpper;
        private final float blendRange;

        BlendNoise(Noise selector, Noise lower, Noise upper, float blendLower, float blendUpper) {
            this.selector = selector;
            this.lower = lower;
            this.upper = upper;
            this.blendLower = blendLower;
            this.blendUpper = blendUpper;
            this.blendRange = blendUpper - blendLower;
        }

        @Override
        public float compute(float x, float z, int seed) {
            float select = selector.compute(x, z, seed);
            if (select <= blendLower) {
                return lower.compute(x, z, seed);
            }
            if (select >= blendUpper) {
                return upper.compute(x, z, seed);
            }
            float alpha = (select - blendLower) / blendRange;
            float lowerValue = lower.compute(x, z, seed);
            float upperValue = upper.compute(x, z, seed);
            return lowerValue + alpha * (upperValue - lowerValue);
        }

        @Override
        public float minValue() {
            return Math.min(lower.minValue(), upper.minValue());
        }

        @Override
        public float maxValue() {
            return Math.max(lower.maxValue(), upper.maxValue());
        }

        @Override
        public Noise mapAll(Visitor visitor) {
            return visitor.apply(new BlendNoise(
                selector.mapAll(visitor),
                lower.mapAll(visitor),
                upper.mapAll(visitor),
                blendLower, blendUpper
            ));
        }
    }
}
