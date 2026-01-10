package org.virgil698.NekoTerraForged.mixin.worldgen.feature.placement;

import org.jetbrains.annotations.Nullable;
import org.virgil698.NekoTerraForged.mixin.bridge.RTFBridge;
import org.virgil698.NekoTerraForged.mixin.bridge.RTFBridgeManager;
import org.virgil698.NekoTerraForged.mixin.worldgen.GeneratorContext;
import org.virgil698.NekoTerraForged.mixin.worldgen.cell.Cell;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementFilter;

/**
 * Cell 过滤器基类
 * 用于基于 RTF Cell 数据的特征放置过滤
 * 移植自 ReTerraForged
 */
public abstract class CellFilter extends PlacementFilter {

    @Override
    protected boolean shouldPlace(PlacementContext ctx, RandomSource rand, BlockPos pos) {
        RTFBridge bridge = RTFBridgeManager.INSTANCE.getBridge();
        if (bridge == null || !bridge.isInitialized()) {
            return false;
        }

        @Nullable
        Object contextObj = bridge.getGeneratorContext();
        if (contextObj instanceof GeneratorContext generatorContext) {
            Cell cell = new Cell();
            generatorContext.getLookup().apply(cell, pos.getX(), pos.getZ());
            return this.shouldPlace(cell, ctx, rand, pos);
        }
        return false;
    }

    protected abstract boolean shouldPlace(Cell cell, PlacementContext ctx, RandomSource rand, BlockPos pos);
}
