package org.virgil698.NekoTerraForged.mixin.worldgen.surface.rule;

import java.util.ArrayList;
import java.util.List;

import com.mojang.serialization.MapCodec;

import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.SurfaceRules;
import net.minecraft.world.level.levelgen.SurfaceRules.Context;

/**
 * 分层表面规则 - 支持多层表面规则组合
 * 移植自 ReTerraForged
 */
public record LayeredSurfaceRule(List<Layer> layers) implements SurfaceRules.RuleSource {
    
    public static final MapCodec<LayeredSurfaceRule> MAP_CODEC = MapCodec.unit(() -> 
        new LayeredSurfaceRule(List.of()));
    
    @Override
    public SurfaceRules.SurfaceRule apply(Context ctx) {
        List<SurfaceRules.SurfaceRule> rules = new ArrayList<>();
        for (Layer layer : this.layers) {
            rules.add(layer.rule().apply(ctx));
        }
        return SurfaceRules.sequence(this.layers.stream()
            .map(Layer::rule)
            .toArray(SurfaceRules.RuleSource[]::new)).apply(ctx);
    }

    @Override
    public KeyDispatchDataCodec<? extends SurfaceRules.RuleSource> codec() {
        return KeyDispatchDataCodec.of(MAP_CODEC);
    }

    public static Layer layer(SurfaceRules.RuleSource rule) {
        return new Layer(rule);
    }
    
    /**
     * 分层配置
     */
    public record Layer(SurfaceRules.RuleSource rule) {
    }
}
