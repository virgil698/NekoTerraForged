package org.virgil698.NekoTerraForged.mixin.worldgen.biome.modifier;

import java.util.Optional;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.HolderSet;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

/**
 * 添加特征的生物群系修改器
 * 用于向生物群系添加新的放置特征
 * 移植自 ReTerraForged
 */
public record AddFeaturesBiomeModifier(
    Order order,
    GenerationStep.Decoration step,
    Optional<Pair<Filter.Behavior, HolderSet<Biome>>> filter,
    HolderSet<PlacedFeature> features
) implements BiomeModifier {
    
    public static final MapCodec<AddFeaturesBiomeModifier> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        Order.CODEC.fieldOf("order").forGetter(AddFeaturesBiomeModifier::order),
        GenerationStep.Decoration.CODEC.fieldOf("step").forGetter(AddFeaturesBiomeModifier::step),
        Codec.pair(
            Filter.Behavior.CODEC.fieldOf("filter_behavior").codec(),
            Biome.LIST_CODEC.fieldOf("biomes").codec()
        ).optionalFieldOf("filter").forGetter(AddFeaturesBiomeModifier::filter),
        PlacedFeature.LIST_CODEC.fieldOf("features").forGetter(AddFeaturesBiomeModifier::features)
    ).apply(instance, AddFeaturesBiomeModifier::new));
    
    @Override
    public MapCodec<? extends BiomeModifier> codec() {
        return CODEC;
    }
}
