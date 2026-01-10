package org.virgil698.NekoTerraForged.mixin.worldgen.feature.template.placement;

import com.mojang.serialization.Codec;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import org.virgil698.NekoTerraForged.mixin.worldgen.feature.template.template.Dimensions;
import org.virgil698.NekoTerraForged.mixin.worldgen.feature.template.template.TemplateContext;

/**
 * 模板放置接口
 * 移植自ReTerraForged
 */
public interface TemplatePlacement<T extends TemplateContext> {
    
    boolean canPlaceAt(LevelAccessor world, BlockPos pos, Dimensions dimensions);

    boolean canReplaceAt(LevelAccessor world, BlockPos pos);
    
    T createContext();
    
    Codec<? extends TemplatePlacement<T>> codec();
}
