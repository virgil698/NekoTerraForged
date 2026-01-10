package org.virgil698.NekoTerraForged.mixin.worldgen.feature.template.placement;

import com.mojang.serialization.Codec;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import org.virgil698.NekoTerraForged.mixin.worldgen.feature.template.BlockUtils;
import org.virgil698.NekoTerraForged.mixin.worldgen.feature.template.decorator.TreeContext;
import org.virgil698.NekoTerraForged.mixin.worldgen.feature.template.template.Dimensions;

/**
 * 树木放置
 * 移植自ReTerraForged
 */
public record TreePlacement() implements TemplatePlacement<TreeContext> {
    public static final Codec<TreePlacement> CODEC = Codec.unit(TreePlacement::new);
    
    @Override
    public boolean canPlaceAt(LevelAccessor world, BlockPos pos, Dimensions dimensions) {
        return BlockUtils.isSoil(world, pos.below()) && BlockUtils.isClearOverhead(world, pos, dimensions.getSizeY(), BlockUtils::canTreeReplace);
    }

    @Override
    public boolean canReplaceAt(LevelAccessor world, BlockPos pos) {
        return BlockUtils.canTreeReplace(world, pos);
    }

    @Override
    public TreeContext createContext() {
        return new TreeContext();
    }

    @Override
    public Codec<TreePlacement> codec() {
        return CODEC;
    }
}
