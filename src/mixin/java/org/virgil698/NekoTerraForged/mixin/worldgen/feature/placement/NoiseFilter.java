package org.virgil698.NekoTerraForged.mixin.worldgen.feature.placement;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementFilter;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

import org.virgil698.NekoTerraForged.mixin.worldgen.noise.module.Noise;

/**
 * 噪声过滤器 - 根据噪声值过滤放置
 * 移植自 ReTerraForged
 */
public class NoiseFilter extends PlacementFilter {

    private Noise noise;
    private float threshold;
    
    public NoiseFilter(Noise noise, float threshold) {
        this.noise = noise;
        this.threshold = threshold;
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
