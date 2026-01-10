package org.virgil698.NekoTerraForged.mixin.registries;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import org.virgil698.NekoTerraForged.mixin.worldgen.biome.modifier.BiomeModifier;
import org.virgil698.NekoTerraForged.mixin.worldgen.feature.chance.ChanceModifier;
import org.virgil698.NekoTerraForged.mixin.worldgen.feature.template.decorator.TemplateDecorator;
import org.virgil698.NekoTerraForged.mixin.worldgen.feature.template.placement.TemplatePlacement;
import org.virgil698.NekoTerraForged.mixin.worldgen.noise.domain.Domain;
import org.virgil698.NekoTerraForged.mixin.worldgen.noise.function.CurveFunction;
import org.virgil698.NekoTerraForged.mixin.worldgen.noise.module.Noise;
import org.virgil698.NekoTerraForged.mixin.worldgen.structure.rule.StructureRule;
import org.virgil698.NekoTerraForged.mixin.worldgen.surface.rule.LayeredSurfaceRule;

/**
 * RTF 注册表键定义
 * 移植自 ReTerraForged RTFRegistries
 */
public class RTFRegistryKeys {
    public static final String NAMESPACE = "nekoterraforged";

    // 类型注册表键
    public static final ResourceKey<Registry<Codec<? extends Noise>>> NOISE_TYPE = createKey("worldgen/noise_type");
    public static final ResourceKey<Registry<Codec<? extends Domain>>> DOMAIN_TYPE = createKey("worldgen/domain_type");
    public static final ResourceKey<Registry<Codec<? extends CurveFunction>>> CURVE_FUNCTION_TYPE = createKey("worldgen/curve_function_type");
    public static final ResourceKey<Registry<MapCodec<? extends ChanceModifier>>> CHANCE_MODIFIER_TYPE = createKey("worldgen/chance_modifier_type");
    public static final ResourceKey<Registry<Codec<? extends TemplatePlacement<?>>>> TEMPLATE_PLACEMENT_TYPE = createKey("worldgen/template_placement_type");
    public static final ResourceKey<Registry<Codec<? extends TemplateDecorator<?>>>> TEMPLATE_DECORATOR_TYPE = createKey("worldgen/template_decorator_type");
    public static final ResourceKey<Registry<MapCodec<? extends BiomeModifier>>> BIOME_MODIFIER_TYPE = createKey("worldgen/biome_modifier_type");
    public static final ResourceKey<Registry<Codec<? extends StructureRule>>> STRUCTURE_RULE_TYPE = createKey("worldgen/structure_rule_type");

    // 数据注册表键
    public static final ResourceKey<Registry<Noise>> NOISE = createKey("worldgen/noise");
    public static final ResourceKey<Registry<BiomeModifier>> BIOME_MODIFIER = createKey("worldgen/biome_modifier");
    public static final ResourceKey<Registry<StructureRule>> STRUCTURE_RULE = createKey("worldgen/structure_rule");
    public static final ResourceKey<Registry<LayeredSurfaceRule.Layer>> SURFACE_LAYERS = createKey("worldgen/surface_layers");

    /**
     * 创建资源位置
     */
    public static ResourceLocation location(String path) {
        return ResourceLocation.fromNamespaceAndPath(NAMESPACE, path);
    }

    /**
     * 创建注册表键
     */
    public static <T> ResourceKey<T> createKey(ResourceKey<? extends Registry<T>> registryKey, String valueKey) {
        return ResourceKey.create(registryKey, location(valueKey));
    }

    private static <T> ResourceKey<Registry<T>> createKey(String key) {
        return ResourceKey.createRegistryKey(location(key));
    }
}
