package org.virgil698.NekoTerraForged.mixin.worldgen.noise.module;

import org.virgil698.NekoTerraForged.mixin.worldgen.util.PosUtil;

/**
 * 2D 缓存噪声
 * 移植自 ReTerraForged
 */
public class Cache2d implements Noise {
    private final Noise noise;
    private final ThreadLocal<Cached> cache;

    public Cache2d(Noise noise) {
        this.noise = noise;
        this.cache = ThreadLocal.withInitial(() -> new Cached(noise));
    }

    public Noise noise() {
        return noise;
    }

    @Override
    public float compute(float x, float z, int seed) {
        return this.cache.get().compute(x, z, seed);
    }

    @Override
    public float minValue() {
        return this.noise.minValue();
    }

    @Override
    public float maxValue() {
        return this.noise.maxValue();
    }

    @Override
    public Noise mapAll(Visitor visitor) {
        return visitor.apply(new Cache2d(this.noise.mapAll(visitor)));
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Cache2d other && other.noise.equals(this.noise);
    }

    /**
     * 缓存的噪声实现
     */
    public static class Cached implements Noise {
        public Noise noise;
        public long lastPos = Long.MIN_VALUE;
        public float value;

        public Cached(Noise noise) {
            this.noise = noise;
        }

        @Override
        public float compute(float x, float z, int seed) {
            long newPos = PosUtil.packf(x, z);
            if (this.lastPos != newPos) {
                this.value = this.noise.compute(x, z, seed);
                this.lastPos = newPos;
            }
            return this.value;
        }

        @Override
        public float minValue() {
            return this.noise.minValue();
        }

        @Override
        public float maxValue() {
            return this.noise.maxValue();
        }

        @Override
        public Noise mapAll(Visitor visitor) {
            return visitor.apply(this);
        }
    }
}
