package org.virgil698.NekoTerraForged.mixin.worldgen.biome.modifier;

import java.util.Map;
import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

/**
 * 替换特征的生物群系修改器
 * 用于替换生物群系中的现有放置特征
 * 移植自 ReTerraForged
 */
public record ReplaceFeaturesBiomeModifier(
    GenerationStep.Decoration step,
    Optional<HolderSet<Biome>> biomes,
    Map<ResourceKey<PlacedFeature>, Holder<PlacedFeature>> replacements
) implements BiomeModifier {
    
    public static final MapCodec<ReplaceFeaturesBiomeModifier> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        GenerationStep.Decoration.CODEC.fieldOf("step").forGetter(ReplaceFeaturesBiomeModifier::step),
        Biome.LIST_CODEC.optionalFieldOf("biomes").forGetter(ReplaceFeaturesBiomeModifier::biomes),
        Codec.unboundedMap(
            ResourceKey.codec(net.minecraft.core.registries.Registries.PLACED_FEATURE),
            PlacedFeature.CODEC
        ).fieldOf("replacements").forGetter(ReplaceFeaturesBiomeModifier::replacements)
    ).apply(instance, ReplaceFeaturesBiomeModifier::new));
    
    @Override
    public MapCodec<? extends BiomeModifier> codec() {
        return CODEC;
    }
}
