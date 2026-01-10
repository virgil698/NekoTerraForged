package org.virgil698.NekoTerraForged.mixin.worldgen.densityfunction;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.Holder;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.DensityFunction;
import org.virgil698.NekoTerraForged.mixin.worldgen.noise.module.Noise;

/**
 * 噪声采样器密度函数
 * 移植自 ReTerraForged
 */
public class NoiseSampler implements DensityFunction {
    private final Noise noise;
    private final int seed;

    public NoiseSampler(Noise noise, int seed) {
        this.noise = noise;
        this.seed = seed;
    }

    public Noise noise() {
        return noise;
    }

    public int seed() {
        return seed;
    }

    @Override
    public double compute(FunctionContext ctx) {
        return this.noise.compute(ctx.blockX(), ctx.blockZ(), this.seed);
    }

    @Override
    public void fillArray(double[] array, ContextProvider contextProvider) {
        contextProvider.fillAllDirectly(array, this);
    }

    @Override
    public DensityFunction mapAll(Visitor visitor) {
        return visitor.apply(this);
    }

    @Override
    public double minValue() {
        return this.noise.minValue();
    }

    @Override
    public double maxValue() {
        return this.noise.maxValue();
    }

    @Override
    public KeyDispatchDataCodec<? extends DensityFunction> codec() {
        throw new UnsupportedOperationException("NoiseSampler does not support codec");
    }

    /**
     * 标记类，用于在 NoiseRouter 中标识需要替换的密度函数
     */
    public static class Marker implements DensityFunction.SimpleFunction {
        public static final MapCodec<Marker> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Noise.DIRECT_CODEC.fieldOf("noise").forGetter(m -> m.noise)
        ).apply(instance, Marker::new));

        private final Noise noise;

        public Marker(Noise noise) {
            this.noise = noise;
        }

        public Noise noise() {
            return noise;
        }

        @Override
        public double compute(FunctionContext ctx) {
            return 0.0;
        }

        @Override
        public double minValue() {
            return noise.minValue();
        }

        @Override
        public double maxValue() {
            return noise.maxValue();
        }

        @Override
        public KeyDispatchDataCodec<Marker> codec() {
            return KeyDispatchDataCodec.of(MAP_CODEC);
        }
    }
}
