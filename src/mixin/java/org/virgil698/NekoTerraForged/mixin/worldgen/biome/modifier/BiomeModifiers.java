package org.virgil698.NekoTerraForged.mixin.worldgen.biome.modifier;

import java.util.Map;
import java.util.Optional;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.MapCodec;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

import org.virgil698.NekoTerraForged.mixin.registries.RTFBuiltInRegistries;

/**
 * 生物群系修改器注册与工厂
 * 移植自 ReTerraForged
 */
public class BiomeModifiers {
    public static final String MOD_ID = "nekoterraforged";
    
    private static boolean registered = false;

    /**
     * 初始化并注册所有生物群系修改器类型
     * 应在插件启动时调用
     */
    public static void bootstrap() {
        if (registered) {
            return;
        }
        registered = true;
        
        register("add_features", AddFeaturesBiomeModifier.CODEC);
        register("replace_features", ReplaceFeaturesBiomeModifier.CODEC);
    }

    // ==================== 添加特征工厂方法 ====================

    /**
     * 创建添加特征修改器（无过滤器）
     */
    @SafeVarargs
    public static BiomeModifier add(Order order, GenerationStep.Decoration step, Holder<PlacedFeature>... features) {
        return add(order, step, HolderSet.direct(features));
    }
    
    /**
     * 创建添加特征修改器（无过滤器）
     */
    public static BiomeModifier add(Order order, GenerationStep.Decoration step, HolderSet<PlacedFeature> features) {
        return add(order, step, Optional.empty(), features);
    }

    /**
     * 创建添加特征修改器（带过滤器）
     */
    @SafeVarargs
    public static BiomeModifier add(Order order, GenerationStep.Decoration step, Filter.Behavior filterBehavior, HolderSet<Biome> biomes, Holder<PlacedFeature>... features) {
        return add(order, step, filterBehavior, biomes, HolderSet.direct(features));
    }

    /**
     * 创建添加特征修改器（带过滤器）
     */
    public static BiomeModifier add(Order order, GenerationStep.Decoration step, Filter.Behavior filterBehavior, HolderSet<Biome> biomes, HolderSet<PlacedFeature> features) {
        return add(order, step, Optional.of(Pair.of(filterBehavior, biomes)), features);
    }
    
    /**
     * 创建添加特征修改器（完整参数）
     */
    public static BiomeModifier add(Order order, GenerationStep.Decoration step, Optional<Pair<Filter.Behavior, HolderSet<Biome>>> filter, HolderSet<PlacedFeature> features) {
        return new AddFeaturesBiomeModifier(order, step, filter, features);
    }

    // ==================== 替换特征工厂方法 ====================

    /**
     * 创建替换特征修改器（无生物群系过滤）
     */
    public static BiomeModifier replace(GenerationStep.Decoration step, Map<ResourceKey<PlacedFeature>, Holder<PlacedFeature>> replacements) {
        return replace(step, Optional.empty(), replacements);
    }

    /**
     * 创建替换特征修改器（带生物群系过滤）
     */
    public static BiomeModifier replace(GenerationStep.Decoration step, HolderSet<Biome> biomes, Map<ResourceKey<PlacedFeature>, Holder<PlacedFeature>> replacements) {
        return replace(step, Optional.of(biomes), replacements);
    }
    
    /**
     * 创建替换特征修改器（完整参数）
     */
    public static BiomeModifier replace(GenerationStep.Decoration step, Optional<HolderSet<Biome>> biomes, Map<ResourceKey<PlacedFeature>, Holder<PlacedFeature>> replacements) {
        return new ReplaceFeaturesBiomeModifier(step, biomes, replacements);
    }

    // ==================== 注册方法 ====================

    /**
     * 注册生物群系修改器类型
     */
    public static void register(String name, MapCodec<? extends BiomeModifier> codec) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(MOD_ID, name);
        Registry.register(RTFBuiltInRegistries.BIOME_MODIFIER_TYPE, id, codec);
    }
    
    /**
     * 获取资源位置
     */
    public static ResourceLocation id(String name) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, name);
    }
}
