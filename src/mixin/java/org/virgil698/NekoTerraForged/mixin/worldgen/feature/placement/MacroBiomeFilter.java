package org.virgil698.NekoTerraForged.mixin.worldgen.feature.placement;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

import org.virgil698.NekoTerraForged.mixin.worldgen.cell.Cell;

/**
 * 宏观生物群系过滤器
 * 移植自 ReTerraForged
 */
public class MacroBiomeFilter extends CellFilter {

    private float chance;
    
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
