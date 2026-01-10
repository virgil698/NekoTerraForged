package org.virgil698.NekoTerraForged.mixin.worldgen.feature.placement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementFilter;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

import org.virgil698.NekoTerraForged.mixin.worldgen.noise.module.Noise;
import org.virgil698.NekoTerraForged.mixin.worldgen.noise.module.Noises;

/**
 * 噪声过滤器 - 根据噪声值过滤放置
 * 移植自 ReTerraForged
 */
public class NoiseFilter extends PlacementFilter {
    // 简化的 CODEC，使用内联噪声定义
    public static final MapCodec<NoiseFilter> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        Codec.INT.fieldOf("noise_seed").forGetter(filter -> filter.noiseSeed),
        Codec.INT.fieldOf("noise_scale").forGetter(filter -> filter.noiseScale),
        Codec.FLOAT.fieldOf("threshold").forGetter(filter -> filter.threshold)
    ).apply(instance, NoiseFilter::new));

    private final Noise noise;
    private final float threshold;
    private final int noiseSeed;
    private final int noiseScale;

    public NoiseFilter(Noise noise, float threshold) {
        this.noise = noise;
        this.threshold = threshold;
        this.noiseSeed = 0;
        this.noiseScale = 100;
    }

    public NoiseFilter(int noiseSeed, int noiseScale, float threshold) {
        this.noiseSeed = noiseSeed;
        this.noiseScale = noiseScale;
        this.threshold = threshold;
        this.noise = Noises.perlin(noiseSeed, noiseScale, 2);
    }

    @Override
    protected boolean shouldPlace(PlacementContext ctx, RandomSource rand, BlockPos pos) {
        return this.noise.compute(pos.getX(), pos.getZ(), (int) ctx.getLevel().getSeed()) > this.threshold;
    }

    @Override
    public PlacementModifierType<NoiseFilter> type() {
        return RTFPlacementModifiers.NOISE_FILTER;
    }

    public Noise getNoise() {
        return noise;
    }

    public float getThreshold() {
        return threshold;
    }
}
