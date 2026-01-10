package org.virgil698.NekoTerraForged.mixin.worldgen.noise.module;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;

import net.minecraft.util.Mth;
import org.virgil698.NekoTerraForged.mixin.worldgen.noise.NoiseUtil;

/**
 * 线性样条噪声模块
 * 移植自 ReTerraForged
 */
public record LinearSpline(Noise input, List<Pair<Float, Noise>> points, float minValue, float maxValue) implements Noise {
    
    public LinearSpline(Noise input, List<Pair<Float, Noise>> points) {
        this(input, points, min(points), max(points));
    }
    
    @Override
    public float compute(float x, float z, int seed) {
        float inputVal = this.input.compute(x, z, seed);
        
        int pointCount = this.points.size();
        Pair<Float, Noise> first = this.points.get(0);
        Pair<Float, Noise> last = this.points.get(pointCount - 1);
        
        if (inputVal <= first.getFirst()) {
            return first.getSecond().compute(x, z, seed);
        }
        
        if (inputVal >= last.getFirst()) {
            return last.getSecond().compute(x, z, seed);
        }
        
        int index = Mth.binarySearch(0, pointCount, i -> inputVal < this.points.get(i).getFirst()) - 1;
        Pair<Float, Noise> start = this.points.get(index);
        Pair<Float, Noise> end = this.points.get(index + 1);
        float min = start.getFirst();
        float max = end.getFirst();
        float from = start.getSecond().compute(x, z, seed);
        float to = end.getSecond().compute(x, z, seed);
        
        float lerp = NoiseUtil.map(inputVal, 0.0F, 1.0F, min, max);
        lerp = NoiseUtil.clamp(lerp, 0.0F, 1.0F);
        
        return NoiseUtil.lerp(from, to, lerp);
    }

    @Override
    public Noise mapAll(Visitor visitor) {
        return visitor.apply(new LinearSpline(this.input.mapAll(visitor), this.points.stream().map((point) -> {
            return Pair.of(point.getFirst(), visitor.apply(point.getSecond()));
        }).toList()));
    }

    public static LinearSpline.Builder builder(Noise noise) {
        return new LinearSpline.Builder(noise);
    }
    
    private static float min(List<Pair<Float, Noise>> points) {
        return (float) points.stream().map(Pair::getSecond).mapToDouble(Noise::minValue).min().orElseThrow();
    }
    
    private static float max(List<Pair<Float, Noise>> points) {
        return (float) points.stream().map(Pair::getSecond).mapToDouble(Noise::maxValue).max().orElseThrow();
    }
    
    public static class Builder {
        private Noise input;
        private List<Pair<Float, Noise>> points;
        
        public Builder(Noise input) {
            this.input = input;
            this.points = new ArrayList<>();
        }
        
        public Builder addPoint(float point, float value) {
            return this.addPoint(point, Noises.constant(value));
        }

        public Builder addPoint(float point, Noise value) {
            this.points.add(Pair.of(point, value));
            return this;
        }
        
        public LinearSpline build() {
            return new LinearSpline(this.input, ImmutableList.copyOf(this.points));
        }
    }
}
