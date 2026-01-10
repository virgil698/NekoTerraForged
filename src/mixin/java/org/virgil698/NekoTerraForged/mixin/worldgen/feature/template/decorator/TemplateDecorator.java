package org.virgil698.NekoTerraForged.mixin.worldgen.feature.template.decorator;

import com.mojang.serialization.Codec;

import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import org.virgil698.NekoTerraForged.mixin.worldgen.feature.template.template.TemplateContext;

/**
 * 模板装饰器接口
 * 移植自ReTerraForged
 */
public interface TemplateDecorator<T extends TemplateContext> {
    
    void apply(LevelAccessor level, T buffer, RandomSource random, boolean modified);
    
    Codec<? extends TemplateDecorator<T>> codec();
}
