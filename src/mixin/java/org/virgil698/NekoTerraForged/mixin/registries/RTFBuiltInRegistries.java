package org.virgil698.NekoTerraForged.mixin.registries;

import com.mojang.serialization.Codec;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.WritableRegistry;
import net.minecraft.resources.ResourceKey;

import org.virgil698.NekoTerraForged.mixin.worldgen.biome.modifier.BiomeModifier;
import org.virgil698.NekoTerraForged.mixin.worldgen.feature.chance.ChanceModifier;
import org.virgil698.NekoTerraForged.mixin.worldgen.feature.template.decorator.TemplateDecorator;
import org.virgil698.NekoTerraForged.mixin.worldgen.feature.template.placement.TemplatePlacement;
import org.virgil698.NekoTerraForged.mixin.worldgen.noise.domain.Domain;
import org.virgil698.NekoTerraForged.mixin.worldgen.noise.function.CurveFunction;
import org.virgil698.NekoTerraForged.mixin.worldgen.noise.module.Noise;
import org.virgil698.NekoTerraForged.mixin.worldgen.structure.rule.StructureRule;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * RTF 内置注册表
 * 移植自 ReTerraForged RTFBuiltInRegistries
 */
public class RTFBuiltInRegistries {
    
    // 存储所有注册表的映射
    private static final Map<ResourceKey<?>, Registry<?>> REGISTRIES = new ConcurrentHashMap<>();

    // 类型注册表
    public static final Registry<Codec<? extends Noise>> NOISE_TYPE = createRegistry(RTFRegistryKeys.NOISE_TYPE);
    public static final Registry<Codec<? extends Domain>> DOMAIN_TYPE = createRegistry(RTFRegistryKeys.DOMAIN_TYPE);
    public static final Registry<Codec<? extends CurveFunction>> CURVE_FUNCTION_TYPE = createRegistry(RTFRegistryKeys.CURVE_FUNCTION_TYPE);
    public static final Registry<Codec<? extends ChanceModifier>> CHANCE_MODIFIER_TYPE = createRegistry(RTFRegistryKeys.CHANCE_MODIFIER_TYPE);
    public static final Registry<Codec<? extends TemplatePlacement<?>>> TEMPLATE_PLACEMENT_TYPE = createRegistry(RTFRegistryKeys.TEMPLATE_PLACEMENT_TYPE);
    public static final Registry<Codec<? extends TemplateDecorator<?>>> TEMPLATE_DECORATOR_TYPE = createRegistry(RTFRegistryKeys.TEMPLATE_DECORATOR_TYPE);
    public static final Registry<Codec<? extends BiomeModifier>> BIOME_MODIFIER_TYPE = createRegistry(RTFRegistryKeys.BIOME_MODIFIER_TYPE);
    public static final Registry<Codec<? extends StructureRule>> STRUCTURE_RULE_TYPE = createRegistry(RTFRegistryKeys.STRUCTURE_RULE_TYPE);

    /**
     * 创建一个简单的注册表
     */
    @SuppressWarnings("unchecked")
    private static <T> Registry<T> createRegistry(ResourceKey<? extends Registry<T>> key) {
        WritableRegistry<T> registry = new MappedRegistry<>(key, com.mojang.serialization.Lifecycle.stable());
        REGISTRIES.put(key, registry);
        return registry;
    }

    /**
     * 获取注册表
     */
    @SuppressWarnings("unchecked")
    public static <T> Registry<T> getRegistry(ResourceKey<? extends Registry<T>> key) {
        return (Registry<T>) REGISTRIES.get(key);
    }

    /**
     * 初始化所有注册表
     */
    public static void bootstrap() {
        // 注册表在静态初始化时已创建
        // 这里可以添加额外的初始化逻辑
    }
}
