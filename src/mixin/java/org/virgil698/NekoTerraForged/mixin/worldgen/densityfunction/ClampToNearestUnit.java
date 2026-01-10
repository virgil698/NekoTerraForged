package org.virgil698.NekoTerraForged.mixin.worldgen.densityfunction;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.DensityFunction;

/**
 * 钳制到最近单位的密度函数
 * 移植自 ReTerraForged
 */
public record ClampToNearestUnit(DensityFunction function, int resolution) implements DensityFunction {
    public static final MapCodec<ClampToNearestUnit> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        DensityFunction.HOLDER_HELPER_CODEC.fieldOf("function").forGetter(ClampToNearestUnit::function),
        com.mojang.serialization.Codec.INT.fieldOf("resolution").forGetter(ClampToNearestUnit::resolution)
    ).apply(instance, ClampToNearestUnit::new));

    @Override
    public double compute(FunctionContext ctx) {
        return this.computeClamped(this.function.compute(ctx));
    }

    @Override
    public void fillArray(double[] arr, ContextProvider ctx) {
        this.function.fillArray(arr, ctx);
        for (int i = 0; i < arr.length; i++) {
            arr[i] = this.computeClamped(arr[i]);
        }
    }

    @Override
    public DensityFunction mapAll(Visitor visitor) {
        return visitor.apply(new ClampToNearestUnit(this.function.mapAll(visitor), this.resolution));
    }

    @Override
    public double minValue() {
        return this.computeClamped(this.function.minValue());
    }

    @Override
    public double maxValue() {
        return this.computeClamped(this.function.maxValue());
    }

    @Override
    public KeyDispatchDataCodec<ClampToNearestUnit> codec() {
        throw new UnsupportedOperationException("Codec not supported in plugin context");
    }

    private double computeClamped(double value) {
        float scaled = (int) (value * this.resolution) + 1;
        return (scaled / this.resolution);
    }
}
