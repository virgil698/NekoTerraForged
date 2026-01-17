package org.virgil698.NekoTerraForged.mixin.world.river;

import org.virgil698.NekoTerraForged.mixin.math.Mth;
import org.virgil698.NekoTerraForged.mixin.math.Spline;

import java.util.Arrays;
import java.util.function.IntFunction;

/**
 * 河流系统
 * 移植自 Valley
 */
public class River {
    public static final int EVAL_RADIUS = 2;
    public static final int EVAL_WIDTH = 5;
    public static final int SAMPLE_RADIUS = 4;
    public static final int SAMPLE_WIDTH = 9;
    public static final int LOCAL_CACHE_SIZE = 8;
    public static final int SHARED_CACHE_SIZE = 256;
    public static final int[] EVAL_CELLS = InitEvalCells(new int[25]);
    public static final int[] CONNECTION_CELLS = InitConnections(new int[4]);
    public static final int[][] SAMPLE_OFFSETS = InitOffsets(new int[81][2]);
    public static final Config DEFAULTS = Config.Defaults();

    /**
     * 河流配置
     */
    public static class Config {
        private final int scale;
        private final double freq;
        private final double ocean;
        private final double coast;
        private final Spline riverRadius;
        private final Spline valleyRadius;

        public Config(int scale, double freq, double ocean, double coast, Spline riverRadius, Spline valleyRadius) {
            this.scale = scale;
            this.freq = freq;
            this.ocean = ocean;
            this.coast = coast;
            this.riverRadius = riverRadius;
            this.valleyRadius = valleyRadius;
        }

        public Config(int scale, double ocean, double coast, Spline riverRadius, Spline valleyRadius) {
            this(scale, 1.0 / Mth.Max(scale, 1.0), ocean, coast, riverRadius, valleyRadius);
        }

        public int scale() {
            return scale;
        }

        public double freq() {
            return freq;
        }

        public double ocean() {
            return ocean;
        }

        public double coast() {
            return coast;
        }

        public Spline riverRadius() {
            return riverRadius;
        }

        public Spline valleyRadius() {
            return valleyRadius;
        }

        public static Config Defaults() {
            return new Config(300, -0.25, -0.19, RiverRadius(), ValleyRadius());
        }

        public static Spline RiverRadius() {
            return Spline.Of(new double[][]{
                {-0.19, 0.15},
                {0.1, 0.1},
                {1.0, 0.03}
            });
        }

        public static Spline ValleyRadius() {
            return Spline.Of(new double[][]{
                {-0.19, 1.0},
                {1.0, 0.8}
            });
        }
    }

    /**
     * 河流采样器
     */
    public static class Sampler {
        public static final int SIZE = 81;
        public static final ThreadLocal<Sampler> LOCAL = ThreadLocal.withInitial(Sampler::new);
        
        public double cachedValue = 0.0;
        public int cachedX = Integer.MIN_VALUE;
        public int cachedZ = Integer.MIN_VALUE;
        public final int[] hash = new int[81];
        public final double[] x = new double[81];
        public final double[] y = new double[81];
        public final double[] noise = new double[81];
        public final Region.Buffer buffer = new Region.Buffer();
        private final LinearCache<Region> localCache = new LinearCache<>(8, x$0 -> new Region[x$0]);

        public Region region(int x, int z, RegionCache sharedCache) {
            return localCache.computeIfAbsent(Mth.Pack(x, z), sharedCache.lookup);
        }
    }

    /**
     * 河流段
     */
    public static class Segment {
        private static final int AX = 0;
        private static final int AY = 1;
        private static final int BX = 2;
        private static final int BY = 3;
        private static final int AN = 4;
        private static final int BN = 5;
        private static final int WARP = 6;
        private final double[] data = new double[7];

        public double project(double x, double y) {
            double bax = data[BX] - data[AX];
            double bay = data[BY] - data[AY];
            double dot = ((x - data[AX]) * bax) + ((y - data[AY]) * bay);
            double prod = (bax * bax) + (bay * bay);
            return prod == 0.0 ? 0.0 : dot / prod;
        }

        public double noise(double t) {
            return t <= 0.0 ? data[AN] : t >= 1.0 ? data[BN] : data[AN] + ((data[BN] - data[AN]) * t);
        }

        public double dist2(double x, double y, double t) {
            double warp = Mth.Clamp((t - 0.05) / 0.9, 0.0, 1.0);
            warp = warp < 0.5 ? warp / 0.5 : (1.0 - warp) / 0.5;
            warp = warp * warp * (3.0 - 2.0 * warp); // Interp3
            double warp2 = warp * data[WARP];
            
            double px = t <= 0.0 ? data[AX] : t >= 1.0 ? data[BX] : data[AX] + ((data[BX] - data[AX]) * t);
            double py = t <= 0.0 ? data[AY] : t >= 1.0 ? data[BY] : data[AY] + ((data[BY] - data[AY]) * t);
            double wx = px + (warp2 * (data[BY] - data[AY]));
            double wy = py - (warp2 * (data[BX] - data[AX]));
            double dx = x - wx;
            double dy = y - wy;
            double d2 = (dx * dx) + (dy * dy);
            return Mth.Min(d2, 1.0);
        }

        public static Segment Of(double ax, double ay, double an, double bx, double by, double bn, double warp) {
            Segment segment = new Segment();
            segment.data[AX] = ax;
            segment.data[AY] = ay;
            segment.data[AN] = an;
            segment.data[BX] = bx;
            segment.data[BY] = by;
            segment.data[BN] = bn;
            segment.data[WARP] = warp;
            return segment;
        }
    }

    /**
     * 河流区域
     */
    public static class Region {
        private static final int INITIAL_CAPACITY = 16;
        private static final Segment[] EMPTY = new Segment[0];
        public int size = 0;
        public Segment[] segments = EMPTY;

        public synchronized void transfer(Buffer buffer) {
            int tail = size;
            int newSize = tail + buffer.region.size;
            if (segments.length < newSize) {
                segments = Arrays.copyOf(segments, newSize);
            }
            for (int i = 0; i < buffer.region.size; i++) {
                segments[tail + i] = buffer.region.segments[i];
                buffer.region.segments[i] = null;
            }
            size = newSize;
            buffer.region.size = 0;
        }

        public static class Buffer {
            private final Region region = new Region();

            public void add(Segment segment) {
                int index = region.size;
                region.size++;
                if (index >= region.segments.length) {
                    int capacity = index == 0 ? INITIAL_CAPACITY : region.segments.length << 1;
                    region.segments = Arrays.copyOf(region.segments, capacity);
                }
                region.segments[index] = segment;
            }
        }
    }

    /**
     * 区域缓存
     */
    public static class RegionCache {
        private final ConcurrentCache<Region> cache;
        public final ComputeFunction<Region> lookup = this::lookup;
        private final ComputeFunction<Region> allocate = this::allocate;

        public RegionCache(int size, int concurrency) {
            this.cache = new ConcurrentCache<>(size, concurrency, x$0 -> new Region[x$0]);
        }

        private Region lookup(long key) {
            return cache.computeIfAbsent(key, allocate);
        }

        private Region allocate(long key) {
            return new Region();
        }

        public static RegionCache Create() {
            return new RegionCache(SHARED_CACHE_SIZE, Runtime.getRuntime().availableProcessors());
        }
    }

    // 初始化方法
    private static int[] InitEvalCells(int[] indices) {
        int i = 0;
        for (int y = -2; y <= 2; y++) {
            for (int x = -2; x <= 2; x++) {
                indices[i] = 40 + (9 * y) + x;
                i++;
            }
        }
        return indices;
    }

    private static int[][] InitOffsets(int[][] offsets) {
        int i = 0;
        for (int dy = -4; dy <= 4; dy++) {
            for (int dx = -4; dx <= 4; dx++) {
                offsets[i][0] = dx;
                offsets[i][1] = dy;
                i++;
            }
        }
        return offsets;
    }

    private static int[] InitConnections(int[] indices) {
        indices[0] = -9;
        indices[1] = -1;
        indices[2] = 1;
        indices[3] = 9;
        return indices;
    }

    // 简化的缓存实现
    @FunctionalInterface
    public interface ComputeFunction<V> {
        V apply(long key);
    }

    private static class LinearCache<V> {
        private final long[] keys;
        private final V[] values;
        private int index = 0;

        @SuppressWarnings("unchecked")
        public LinearCache(int size, IntFunction<V[]> factory) {
            this.keys = new long[size];
            this.values = factory.apply(size);
            Arrays.fill(keys, Long.MIN_VALUE);
        }

        public V computeIfAbsent(long key, ComputeFunction<V> function) {
            for (int i = 0; i < keys.length; i++) {
                if (keys[i] == key) {
                    return values[i];
                }
            }
            V value = function.apply(key);
            keys[index] = key;
            values[index] = value;
            index = (index + 1) % keys.length;
            return value;
        }
    }

    private static class ConcurrentCache<V> {
        private final LinearCache<V>[] caches;
        private final int mask;

        @SuppressWarnings("unchecked")
        public ConcurrentCache(int size, int concurrency, IntFunction<V[]> factory) {
            concurrency = Math.max(1, concurrency);
            this.mask = concurrency - 1;
            this.caches = new LinearCache[concurrency];
            int cacheSize = Math.max(1, size / concurrency);
            for (int i = 0; i < concurrency; i++) {
                caches[i] = new LinearCache<>(cacheSize, factory);
            }
        }

        public V computeIfAbsent(long key, ComputeFunction<V> function) {
            int index = (int) (Mth.Mix((int) key) & mask);
            return caches[index].computeIfAbsent(key, function);
        }
    }
}
