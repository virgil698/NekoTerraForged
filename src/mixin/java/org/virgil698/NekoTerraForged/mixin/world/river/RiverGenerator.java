package org.virgil698.NekoTerraForged.mixin.world.river;

import org.virgil698.NekoTerraForged.mixin.math.Mth;
import org.virgil698.NekoTerraForged.mixin.math.Node;
import org.virgil698.NekoTerraForged.mixin.math.Simplex;
import org.virgil698.NekoTerraForged.mixin.math.Spline;

/**
 * 河流生成器
 * 移植自 Valley
 */
public class RiverGenerator {
    public static final double RIVER_OUTER = 0.9;
    public static final double RIVER_INNER = 1.0;
    public static final double VALLEY_OUTER = 0.0;
    public static final double VALLEY_INNER = 0.9;
    public static final double POINT_JITTER = 0.5;
    public static final double WARP_STRENGTH = 0.5;
    public static final int SEGMENT_COUNT = 2;
    public static final double SEGMENT_GAIN = 0.5;
    public static final double SEGMENT_JITTER = 0.5;
    public static final int SIGN_SEED = 716498721;
    public static final double SIGN_FREQ = 4.8828125E-4;
    public static final Node.Warp WARP = RiverWarp();

    public static double Sign(int seed, int x, int z) {
        double fbm = SimplexFBM(seed + SIGN_SEED, x * SIGN_FREQ, z * SIGN_FREQ, 2, 2.0, 0.5);
        return fbm >= VALLEY_OUTER ? 1.0 : -1.0;
    }

    public static double Sample(int seed, int x, int y, Node noise, River.Config config, River.RegionCache cache) {
        River.Sampler sampler = River.Sampler.LOCAL.get();
        if (x == sampler.cachedX && y == sampler.cachedZ) {
            return sampler.cachedValue;
        }
        
        if (noise.eval(seed, x, y) < config.coast()) {
            sampler.cachedX = x;
            sampler.cachedZ = y;
            sampler.cachedValue = VALLEY_OUTER;
            return VALLEY_OUTER;
        }
        
        double result = 0.0;
        double freq = config.freq();
        Spline riverSpline = config.riverRadius();
        Spline valleySpline = config.valleyRadius();
        River.Region region = GenRegion(seed, x * freq, y * freq, noise, config, sampler, cache);
        double px = WARP.pointX(seed, x, y) * freq;
        double py = WARP.pointY(seed, x, y) * freq;
        
        for (int i = 0; i < region.size; i++) {
            double t = region.segments[i].project(px, py);
            double dist2 = region.segments[i].dist2(px, py, t);
            double falloff = region.segments[i].noise(t);
            double riverRadius = riverSpline.eval(falloff);
            
            if (dist2 < riverRadius * riverRadius) {
                double blend = Mth.Sqrt(dist2) / riverRadius;
                result = Math.max(result, Mth.Lerp(1.0, 0.9, blend));
            } else {
                double valleyRadius = valleySpline.eval(falloff);
                if (dist2 < valleyRadius * valleyRadius) {
                    double blend = (Mth.Sqrt(dist2) - riverRadius) / (valleyRadius - riverRadius);
                    result = Math.max(result, Mth.Lerp(0.9, VALLEY_OUTER, blend));
                }
            }
        }
        
        sampler.cachedX = x;
        sampler.cachedZ = y;
        sampler.cachedValue = result;
        return result;
    }

    public static River.Region GenRegion(int seed, double x, double y, Node noise, River.Config config, 
            River.Sampler sampler, River.RegionCache cache) {
        int cx = Mth.Floor(x);
        int cy = Mth.Floor(y);
        
        // 采样周围的点
        for (int i = 0; i < River.SAMPLE_OFFSETS.length; i++) {
            int dx = River.SAMPLE_OFFSETS[i][0];
            int dy = River.SAMPLE_OFFSETS[i][1];
            int hash = Mth.Hash(seed, cx + dx, cy + dy);
            sampler.hash[i] = hash;
            sampler.x[i] = CellPointX(hash, cx + dx, cy + dy, POINT_JITTER);
            sampler.y[i] = CellPointY(hash, cx + dx, cy + dy, POINT_JITTER);
            sampler.noise[i] = noise.eval(seed, sampler.x[i] / config.freq(), sampler.y[i] / config.freq());
        }
        
        // 生成河流段
        for (int i = 0; i < River.EVAL_CELLS.length; i++) {
            int index = River.EVAL_CELLS[i];
            if (sampler.noise[index] < config.ocean()) {
                continue;
            }
            
            River.Region region = sampler.region(cx + River.SAMPLE_OFFSETS[index][0], 
                                                 cy + River.SAMPLE_OFFSETS[index][1], cache);
            
            if (region.size == 0) {
                GenCell(seed, index, sampler, region);
            }
        }
        
        // 合并所有区域
        River.Region result = new River.Region();
        for (int i = 0; i < River.EVAL_CELLS.length; i++) {
            int index = River.EVAL_CELLS[i];
            if (sampler.noise[index] >= config.ocean()) {
                River.Region region = sampler.region(cx + River.SAMPLE_OFFSETS[index][0], 
                                                     cy + River.SAMPLE_OFFSETS[index][1], cache);
                result.transfer(sampler.buffer);
                for (int j = 0; j < region.size; j++) {
                    sampler.buffer.add(region.segments[j]);
                }
            }
        }
        result.transfer(sampler.buffer);
        return result;
    }

    private static void GenCell(int seed, int index, River.Sampler sampler, River.Region region) {
        int outgoing = -1;
        double minDist = Double.MAX_VALUE;
        
        // 找到最近的连接点
        for (int i = 0; i < River.CONNECTION_CELLS.length; i++) {
            int neighbor = index + River.CONNECTION_CELLS[i];
            if (neighbor < 0 || neighbor >= sampler.noise.length) {
                continue;
            }
            if (sampler.noise[neighbor] >= sampler.noise[index]) {
                continue;
            }
            double dist = Mth.Dist2(sampler.x[index], sampler.y[index], 
                                   sampler.x[neighbor], sampler.y[neighbor]);
            if (dist < minDist) {
                minDist = dist;
                outgoing = neighbor;
            }
        }
        
        if (outgoing >= 0) {
            CollectSegments(
                sampler.x[index], sampler.y[index], sampler.noise[index],
                sampler.x[outgoing], sampler.y[outgoing], sampler.noise[outgoing],
                sampler.hash[index], SEGMENT_COUNT, SEGMENT_JITTER, SEGMENT_GAIN,
                sampler.buffer
            );
            region.transfer(sampler.buffer);
        }
    }

    public static void CollectSegments(double ax, double ay, double an, double bx, double by, double bn, 
            int seed, int depth, double scale, double gain, River.Region.Buffer buffer) {
        if (depth > 0) {
            double dx = bx - ax;
            double dy = by - ay;
            double dir = (seed & 1) == 0 ? 0.5 : -0.5;
            double mx = ax + (dx * 0.5) + (dy * dir * scale);
            double my = ay + (dy * 0.5) + (dx * dir * scale);
            double mn = (an + bn) * 0.5;
            CollectSegments(ax, ay, an, mx, my, mn, Mth.Mix(seed - 1), depth - 1, scale * gain, gain, buffer);
            CollectSegments(mx, my, mn, bx, by, bn, Mth.Mix(seed + 1), depth - 1, scale * gain, gain, buffer);
            return;
        }
        double warp = ((Mth.Mix(seed + Mth.PRIME_W) & 1) == 0 ? 0.5 : -0.5) * gain;
        buffer.add(River.Segment.Of(ax, ay, an, bx, by, bn, warp));
    }

    private static Node.Warp RiverWarp() {
        return Node.Warp(Simplex.INSTANCE, Node.Fbm(Simplex.INSTANCE, 2, 2.5, 0.5), 0.05, 3.0);
    }

    private static double CellPointX(int hash, int cx, int cy, double jitter) {
        double jitterX = (hash & 0xFFFF) / 65535.0;
        return cx + (jitterX * jitter);
    }

    private static double CellPointY(int hash, int cx, int cy, double jitter) {
        double jitterY = ((hash >> 16) & 0xFFFF) / 65535.0;
        return cy + (jitterY * jitter);
    }

    private static double SimplexFBM(int seed, double x, double y, int octaves, double lacunarity, double gain) {
        double amplitude = 1.0;
        double frequency = 1.0;
        double sum = 0.0;
        double max = 0.0;
        for (int i = 0; i < octaves; i++) {
            sum += Simplex.Sample(seed, x * frequency, y * frequency) * amplitude;
            max += amplitude;
            amplitude *= gain;
            frequency *= lacunarity;
        }
        return sum / max;
    }
}
