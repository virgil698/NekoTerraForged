package org.virgil698.NekoTerraForged.mixin.worldgen.feature.template.template;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 模板上下文接口
 * 移植自ReTerraForged
 */
public interface TemplateContext {
    void recordState(BlockPos pos, BlockState state);
    
    public interface Factory<T extends TemplateContext> {
        T createContext();
    }
}
