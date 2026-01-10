package org.virgil698.NekoTerraForged.mixin.worldgen.surface.rule;

import java.util.List;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.MapCodec;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.SurfaceRules;
import org.virgil698.NekoTerraForged.mixin.worldgen.noise.module.Noise;
import org.virgil698.NekoTerraForged.mixin.worldgen.surface.rule.StrataRule.Layer;

/**
 * RTF 表面规则注册
 * 移植自 ReTerraForged
 */
public class RTFSurfaceRules {

    public static void bootstrap() {
        register("rtf_layered", LayeredSurfaceRule.MAP_CODEC);
        register("rtf_strata", StrataRule.MAP_CODEC);
        register("rtf_noise", NoiseRule.MAP_CODEC);
    }

    public static LayeredSurfaceRule layered(List<LayeredSurfaceRule.Layer> layers) {
        return new LayeredSurfaceRule(layers);
    }

    public static StrataRule strata(ResourceLocation cacheId, int buffer, int iterations, Noise selector, List<Layer> layers) {
        return new StrataRule(cacheId, buffer, iterations, selector, layers);
    }

    public static NoiseRule noise(Noise noise, List<Pair<Float, SurfaceRules.RuleSource>> rules) {
        return new NoiseRule(noise, rules);
    }

    @SuppressWarnings("unchecked")
    public static void register(String name, MapCodec<? extends SurfaceRules.RuleSource> codec) {
        Registry.register(
            (Registry<MapCodec<? extends SurfaceRules.RuleSource>>) (Registry<?>) BuiltInRegistries.MATERIAL_RULE,
            "nekoterraforged:" + name,
            codec
        );
    }
}
