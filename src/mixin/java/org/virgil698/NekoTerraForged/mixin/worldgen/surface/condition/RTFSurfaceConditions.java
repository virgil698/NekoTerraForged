package org.virgil698.NekoTerraForged.mixin.worldgen.surface.condition;

import java.util.Arrays;
import java.util.stream.Collectors;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.SurfaceRules;
import org.virgil698.NekoTerraForged.mixin.worldgen.noise.module.Noise;
import org.virgil698.NekoTerraForged.mixin.worldgen.noise.module.Noises;
import org.virgil698.NekoTerraForged.mixin.worldgen.terrain.Terrain;

/**
 * RTF 表面条件注册
 * 移植自 ReTerraForged
 */
public class RTFSurfaceConditions {

    public static void bootstrap() {
        register("rtf_mod", ModCondition.MAP_CODEC);
        register("rtf_biome_tag", BiomeTagCondition.MAP_CODEC);
        register("rtf_noise", NoiseCondition.Source.MAP_CODEC);
        register("rtf_terrain", TerrainCondition.Source.MAP_CODEC);
        register("rtf_height", HeightCondition.Source.MAP_CODEC);
        register("rtf_steepness", SteepnessCondition.Source.MAP_CODEC);
        register("rtf_erosion", ErosionCondition.Source.MAP_CODEC);
        register("rtf_sediment", SedimentCondition.Source.MAP_CODEC);
        register("rtf_river_bank", RiverBankCondition.Source.MAP_CODEC);
        register("rtf_height_modification_detection", HeightModificationDetection.Source.MAP_CODEC);
    }

    public static ModCondition modLoaded(String modId) {
        return new ModCondition(modId);
    }

    public static BiomeTagCondition biomeTag(TagKey<Biome> tag) {
        return new BiomeTagCondition(tag);
    }

    public static NoiseCondition.Source noise(Noise noise, float threshold) {
        return new NoiseCondition.Source(noise, threshold);
    }

    public static TerrainCondition.Source terrain(Terrain... terrain) {
        return new TerrainCondition.Source(Arrays.stream(terrain).collect(Collectors.toSet()));
    }

    public static HeightCondition.Source height(float threshold) {
        return height(Noises.constant(threshold));
    }

    public static HeightCondition.Source height(float threshold, Noise variance) {
        return height(Noises.constant(threshold), variance);
    }

    public static HeightCondition.Source height(Noise threshold) {
        return height(threshold, Noises.constant(0.0F));
    }

    public static HeightCondition.Source height(Noise threshold, Noise variance) {
        return new HeightCondition.Source(threshold, variance);
    }

    public static SteepnessCondition.Source steepness(float threshold) {
        return steepness(Noises.constant(threshold), Noises.constant(0.0F));
    }

    public static SteepnessCondition.Source steepness(float threshold, Noise variance) {
        return steepness(Noises.constant(threshold), variance);
    }

    public static SteepnessCondition.Source steepness(Noise threshold, Noise variance) {
        return new SteepnessCondition.Source(threshold, variance);
    }

    public static ErosionCondition.Source erosion(float threshold) {
        return erosion(Noises.constant(threshold), Noises.constant(0.0F));
    }

    public static ErosionCondition.Source erosion(Noise threshold, Noise variance) {
        return new ErosionCondition.Source(threshold, variance);
    }

    public static SedimentCondition.Source sediment(float threshold) {
        return sediment(Noises.constant(threshold), Noises.constant(0.0F));
    }

    public static SedimentCondition.Source sediment(Noise threshold, Noise variance) {
        return new SedimentCondition.Source(threshold, variance);
    }

    public static RiverBankCondition.Source riverBank(float threshold) {
        return riverBank(Noises.constant(threshold), Noises.constant(0.0F));
    }

    public static RiverBankCondition.Source riverBank(Noise threshold, Noise variance) {
        return new RiverBankCondition.Source(threshold, variance);
    }

    public static HeightModificationDetection.Source heightModificationDetection(HeightModificationDetection.Target target) {
        return new HeightModificationDetection.Source(target);
    }

    @SuppressWarnings("unchecked")
    public static void register(String name, MapCodec<? extends SurfaceRules.ConditionSource> codec) {
        Registry.register(
            (Registry<MapCodec<? extends SurfaceRules.ConditionSource>>) (Registry<?>) BuiltInRegistries.MATERIAL_CONDITION,
            "nekoterraforged:" + name,
            codec
        );
    }
}
