package org.virgil698.NekoTerraForged.mixin.worldgen.feature.template.paste;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import org.virgil698.NekoTerraForged.mixin.worldgen.feature.template.placement.TemplatePlacement;
import org.virgil698.NekoTerraForged.mixin.worldgen.feature.template.template.TemplateContext;

/**
 * 粘贴接口
 * 移植自ReTerraForged
 */
public interface Paste {
    <T extends TemplateContext> boolean apply(LevelAccessor world, T ctx, BlockPos origin, Mirror mirror, Rotation rotation, TemplatePlacement<T> placement, PasteConfig config);
}
