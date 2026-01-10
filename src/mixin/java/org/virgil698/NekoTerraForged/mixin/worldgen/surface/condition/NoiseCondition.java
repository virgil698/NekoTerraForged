package org.virgil698.NekoTerraForged.mixin.worldgen.surface.condition;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.SurfaceRules;
import net.minecraft.world.level.levelgen.SurfaceRules.Context;
import org.virgil698.NekoTerraForged.mixin.worldgen.noise.module.Noise;

/**
 * 噪声条件
 * 移植自 ReTerraForged
 */
public class NoiseCondition extends SurfaceRules.LazyXZCondition {
    private Noise noise;
    private float threshold;

    private NoiseCondition(Context context, Noise noise, float threshold) {
        super(context);
        this.noise = noise;
        this.threshold = threshold;
    }

    @Override
    protected boolean compute() {
        return this.noise.compute(this.context.blockX, this.context.blockZ, 0) > this.threshold;
    }

    public record Source(Noise noise, float threshold) implements SurfaceRules.ConditionSource {
        public static final MapCodec<Source> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Noise.DIRECT_CODEC.fieldOf("noise").forGetter(Source::noise),
            Codec.FLOAT.fieldOf("threshold").forGetter(Source::threshold)
        ).apply(instance, Source::new));

        @Override
        public NoiseCondition apply(Context ctx) {
            return new NoiseCondition(ctx, this.noise, this.threshold);
        }

        @Override
        public KeyDispatchDataCodec<Source> codec() {
            return KeyDispatchDataCodec.of(MAP_CODEC);
        }
    }
}
