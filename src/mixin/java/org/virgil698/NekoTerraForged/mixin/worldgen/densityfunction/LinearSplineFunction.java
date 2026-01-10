package org.virgil698.NekoTerraForged.mixin.worldgen.densityfunction;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.MapCodec;

import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;

/**
 * 线性样条密度函数
 * 移植自 ReTerraForged
 */
public record LinearSplineFunction(DensityFunction input, List<Pair<Double, DensityFunction>> points, 
        double minValue, double maxValue) implements DensityFunction {

    public static final MapCodec<LinearSplineFunction> MAP_CODEC = MapCodec.unit(() -> 
        new LinearSplineFunction(DensityFunctions.zero(), List.of()));

    public LinearSplineFunction(DensityFunction input, List<Pair<Double, DensityFunction>> points) {
        this(input, points, min(points), max(points));
    }

    @Override
    public double compute(FunctionContext ctx) {
        double input = this.input.compute(ctx);
        int pointCount = this.points.size();

        Pair<Double, DensityFunction> first = this.points.get(0);
        if (input <= first.getFirst()) {
            return first.getSecond().compute(ctx);
        }

        Pair<Double, DensityFunction> last = this.points.get(pointCount - 1);
        if (input >= last.getFirst()) {
            return last.getSecond().compute(ctx);
        }

        int index = Mth.binarySearch(0, pointCount, i -> input < this.points.get(i).getFirst()) - 1;
        Pair<Double, DensityFunction> start = this.points.get(index);
        Pair<Double, DensityFunction> end = this.points.get(index + 1);
        double min = start.getFirst();
        double max = end.getFirst();
        double from = start.getSecond().compute(ctx);
        double to = end.getSecond().compute(ctx);

        double lerp = (input - min) / (max - min);
        lerp = Math.max(0.0D, Math.min(1.0D, lerp));
        return from + lerp * (to - from);
    }

    @Override
    public void fillArray(double[] array, ContextProvider ctxProvider) {
        ctxProvider.fillAllDirectly(array, this);
    }

    @Override
    public DensityFunction mapAll(Visitor visitor) {
        return visitor.apply(new LinearSplineFunction(this.input.mapAll(visitor), this.points.stream().map((point) -> {
            return Pair.of(point.getFirst(), visitor.apply(point.getSecond()));
        }).toList()));
    }

    @Override
    public KeyDispatchDataCodec<LinearSplineFunction> codec() {
        throw new UnsupportedOperationException("Codec not supported in plugin context");
    }

    public static LinearSplineFunction.Builder builder(DensityFunction input) {
        return new LinearSplineFunction.Builder(input);
    }

    private static float min(List<Pair<Double, DensityFunction>> points) {
        return (float) points.stream().map(Pair::getSecond).mapToDouble(DensityFunction::minValue).min().orElseThrow();
    }

    private static float max(List<Pair<Double, DensityFunction>> points) {
        return (float) points.stream().map(Pair::getSecond).mapToDouble(DensityFunction::maxValue).max().orElseThrow();
    }

    public static class Builder {
        private DensityFunction input;
        private List<Pair<Double, DensityFunction>> points;

        public Builder(DensityFunction input) {
            this.input = input;
            this.points = new ArrayList<>();
        }

        public Builder addPoint(double point, double value) {
            return this.addPoint(point, DensityFunctions.constant(value));
        }

        public Builder addPoint(double point, DensityFunction value) {
            this.points.add(Pair.of(point, value));
            return this;
        }

        public LinearSplineFunction build() {
            return new LinearSplineFunction(this.input, ImmutableList.copyOf(this.points));
        }
    }
}
