package org.virgil698.NekoTerraForged.mixin.worldgen.feature.template.template;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 空操作模板上下文
 * 移植自ReTerraForged
 */
public record NoopTemplateContext() implements TemplateContext {

    @Override
    public void recordState(BlockPos pos, BlockState state) {
    }
}
