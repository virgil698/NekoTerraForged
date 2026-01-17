package org.virgil698.NekoTerraForged.mixin.math;

/**
 * 噪声修饰符和组合器
 * 提供额外的噪声操作
 */
public class NoiseModifiers {
    
    /**
     * 绝对值
     */
    public static class Abs implements Node {
        private final Node source;

        public Abs(Node source) {
            this.source = source;
        }

        @Override
        public double eval(int seed, double x, double y) {
            return Math.abs(source.eval(seed, x, y));
        }

        @Override
        public double min() {
            return 0.0;
        }

        @Override
        public double max() {
            return Math.max(Math.abs(source.min()), Math.abs(source.max()));
        }
    }

    /**
     * 加法
     */
    public static class Add implements Node {
        private final Node a;
        private final Node b;

        public Add(Node a, Node b) {
            this.a = a;
            this.b = b;
        }

        @Override
        public double eval(int seed, double x, double y) {
            return a.eval(seed, x, y) + b.eval(seed, x, y);
        }

        @Override
        public double min() {
            return a.min() + b.min();
        }

        @Override
        public double max() {
            return a.max() + b.max();
        }
    }

    /**
     * 乘法
     */
    public static class Multiply implements Node {
        private final Node a;
        private final Node b;

        public Multiply(Node a, Node b) {
            this.a = a;
            this.b = b;
        }

        @Override
        public double eval(int seed, double x, double y) {
            return a.eval(seed, x, y) * b.eval(seed, x, y);
        }

        @Override
        public double min() {
            return Math.min(
                Math.min(a.min() * b.min(), a.min() * b.max()),
                Math.min(a.max() * b.min(), a.max() * b.max())
            );
        }

        @Override
        public double max() {
            return Math.max(
                Math.max(a.min() * b.min(), a.min() * b.max()),
                Math.max(a.max() * b.min(), a.max() * b.max())
            );
        }
    }

    /**
     * 最小值
     */
    public static class Min implements Node {
        private final Node a;
        private final Node b;

        public Min(Node a, Node b) {
            this.a = a;
            this.b = b;
        }

        @Override
        public double eval(int seed, double x, double y) {
            return Math.min(a.eval(seed, x, y), b.eval(seed, x, y));
        }

        @Override
        public double min() {
            return Math.min(a.min(), b.min());
        }

        @Override
        public double max() {
            return Math.min(a.max(), b.max());
        }
    }

    /**
     * 最大值
     */
    public static class Max implements Node {
        private final Node a;
        private final Node b;

        public Max(Node a, Node b) {
            this.a = a;
            this.b = b;
        }

        @Override
        public double eval(int seed, double x, double y) {
            return Math.max(a.eval(seed, x, y), b.eval(seed, x, y));
        }

        @Override
        public double min() {
            return Math.max(a.min(), b.min());
        }

        @Override
        public double max() {
            return Math.max(a.max(), b.max());
        }
    }

    /**
     * 幂运算
     */
    public static class Power implements Node {
        private final Node source;
        private final double power;

        public Power(Node source, double power) {
            this.source = source;
            this.power = power;
        }

        @Override
        public double eval(int seed, double x, double y) {
            return Math.pow(source.eval(seed, x, y), power);
        }

        @Override
        public double min() {
            if (power % 2 == 0) {
                return 0.0;
            }
            return Math.pow(source.min(), power);
        }

        @Override
        public double max() {
            return Math.pow(source.max(), power);
        }
    }

    /**
     * 偏移
     */
    public static class Bias implements Node {
        private final Node source;
        private final double bias;

        public Bias(Node source, double bias) {
            this.source = source;
            this.bias = bias;
        }

        @Override
        public double eval(int seed, double x, double y) {
            return source.eval(seed, x, y) + bias;
        }

        @Override
        public double min() {
            return source.min() + bias;
        }

        @Override
        public double max() {
            return source.max() + bias;
        }
    }

    /**
     * 缩放
     */
    public static class Gain implements Node {
        private final Node source;
        private final double gain;

        public Gain(Node source, double gain) {
            this.source = source;
            this.gain = gain;
        }

        @Override
        public double eval(int seed, double x, double y) {
            return source.eval(seed, x, y) * gain;
        }

        @Override
        public double min() {
            return source.min() * gain;
        }

        @Override
        public double max() {
            return source.max() * gain;
        }
    }

    /**
     * 钳制
     */
    public static class Clamp implements Node {
        private final Node source;
        private final double min;
        private final double max;

        public Clamp(Node source, double min, double max) {
            this.source = source;
            this.min = min;
            this.max = max;
        }

        @Override
        public double eval(int seed, double x, double y) {
            return Mth.Clamp(source.eval(seed, x, y), min, max);
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

    /**
     * 反转
     */
    public static class Invert implements Node {
        private final Node source;

        public Invert(Node source) {
            this.source = source;
        }

        @Override
        public double eval(int seed, double x, double y) {
            return -source.eval(seed, x, y);
        }

        @Override
        public double min() {
            return -source.max();
        }

        @Override
        public double max() {
            return -source.min();
        }
    }
}
