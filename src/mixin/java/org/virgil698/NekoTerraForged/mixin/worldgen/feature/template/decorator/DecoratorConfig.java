package org.virgil698.NekoTerraForged.mixin.worldgen.feature.template.decorator;

import java.util.List;
import java.util.Map;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import org.virgil698.NekoTerraForged.mixin.worldgen.feature.template.template.TemplateContext;

/**
 * 装饰器配置
 * 移植自ReTerraForged
 */
public record DecoratorConfig<T extends TemplateContext>(List<TemplateDecorator<T>> defaultDecorators, Map<ResourceKey<Biome>, List<TemplateDecorator<T>>> biomeDecorators) {
    
    public List<TemplateDecorator<T>> getDecorators(ResourceKey<Biome> biome) {
        if (biome == null) {
            return this.defaultDecorators;
        }
        return this.biomeDecorators.getOrDefault(biome, this.defaultDecorators);
    }
}
