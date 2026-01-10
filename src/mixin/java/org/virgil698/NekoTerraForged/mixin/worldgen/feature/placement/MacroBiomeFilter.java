package org.virgil698.NekoTerraForged.mixin.worldgen.feature.placement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

import org.virgil698.NekoTerraForged.mixin.worldgen.cell.Cell;

/**
 * 宏观生物群系过滤器
 * 基于 Cell 的 macroBiomeId 过滤特征放置
 * 移植自 ReTerraForged
 */
public class MacroBiomeFilter extends CellFilter {
    public static final MapCodec<MacroBiomeFilter> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        Codec.FLOAT.fieldOf("chance").forGetter(filter -> filter.chance)
    ).apply(instance, MacroBiomeFilter::new));

    private final float chance;

    public MacroBiomeFilter(float chance) {
        this.chance = chance;
    }

    @Override
    protected boolean shouldPlace(Cell cell, PlacementContext ctx, RandomSource rand, BlockPos pos) {
        return cell.macroBiomeId > (1.0F - this.chance);
    }

    @Override
    public PlacementModifierType<MacroBiomeFilter> type() {
        return RTFPlacementModifiers.MACRO_BIOME_FILTER;
    }

    public float getChance() {
        return chance;
    }
}
