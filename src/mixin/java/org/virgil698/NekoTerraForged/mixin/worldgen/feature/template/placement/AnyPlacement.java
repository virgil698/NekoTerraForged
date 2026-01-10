package org.virgil698.NekoTerraForged.mixin.worldgen.feature.template.placement;

import com.mojang.serialization.Codec;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import org.virgil698.NekoTerraForged.mixin.worldgen.feature.template.BlockUtils;
import org.virgil698.NekoTerraForged.mixin.worldgen.feature.template.template.Dimensions;
import org.virgil698.NekoTerraForged.mixin.worldgen.feature.template.template.NoopTemplateContext;

/**
 * 任意放置
 * 移植自ReTerraForged
 */
public record AnyPlacement() implements TemplatePlacement<NoopTemplateContext> {
    public static final Codec<AnyPlacement> CODEC = Codec.unit(AnyPlacement::new);

    @Override
    public boolean canPlaceAt(LevelAccessor world, BlockPos pos, Dimensions dimensions) {
        return true;
    }

    @Override
    public boolean canReplaceAt(LevelAccessor world, BlockPos pos) {
        return !BlockUtils.isSolid(world, pos);
    }

    @Override
    public NoopTemplateContext createContext() {
        return new NoopTemplateContext();
    }

    @Override
    public Codec<AnyPlacement> codec() {
        return CODEC;
    }
}
