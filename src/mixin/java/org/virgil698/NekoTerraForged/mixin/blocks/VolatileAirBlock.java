package org.virgil698.NekoTerraForged.mixin.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 易变空气方块
 * 用于模板放置时的临时占位
 * 移植自 ReTerraForged
 */
public class VolatileAirBlock extends AirBlock {

    public VolatileAirBlock(Properties properties) {
        super(properties);
    }

    /**
     * 更新形状时不触发邻居更新
     * 防止在模板放置过程中产生不必要的方块更新
     */
    @Override
    protected BlockState updateShape(
            BlockState state,
            LevelReader level,
            ScheduledTickAccess scheduledTickAccess,
            BlockPos pos,
            Direction direction,
            BlockPos neighborPos,
            BlockState neighborState,
            RandomSource random) {
        return state;
    }
}
