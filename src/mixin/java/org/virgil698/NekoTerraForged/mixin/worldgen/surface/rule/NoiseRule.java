package org.virgil698.NekoTerraForged.mixin.worldgen.surface.rule;

import java.util.List;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.MapCodec;

import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.SurfaceRules;
import net.minecraft.world.level.levelgen.SurfaceRules.Context;

import org.virgil698.NekoTerraForged.mixin.worldgen.noise.module.Noise;
import org.virgil698.NekoTerraForged.mixin.worldgen.util.PosUtil;

/**
 * 噪声规则 - 根据噪声值选择不同的表面规则
 * 移植自 ReTerraForged
 */
public record NoiseRule(Noise noise, List<Pair<Float, SurfaceRules.RuleSource>> rules) implements SurfaceRules.RuleSource {
    
    public static final MapCodec<NoiseRule> MAP_CODEC = MapCodec.unit(() -> 
        new NoiseRule(Noise.ConstantNoise.ZERO, List.of()));
    
    @Override
    public SurfaceRules.SurfaceRule apply(Context ctx) {
        return new Rule(this.noise, this.rules.stream().map((pair) -> {
            return Pair.of(pair.getFirst(), pair.getSecond().apply(ctx));
        }).sorted((p1, p2) -> p2.getFirst().compareTo(p1.getFirst())).toList());
    }

    @Override
    public KeyDispatchDataCodec<? extends SurfaceRules.RuleSource> codec() {
        return KeyDispatchDataCodec.of(MAP_CODEC);
    }
    
    private static class Rule implements SurfaceRules.SurfaceRule {
        private Noise noise;
        private List<Pair<Float, SurfaceRules.SurfaceRule>> rules;
        private long lastPos;
        private SurfaceRules.SurfaceRule rule;
        
        public Rule(Noise noise, List<Pair<Float, SurfaceRules.SurfaceRule>> rules) {
            this.noise = noise;
            this.rules = rules;
            this.lastPos = Long.MIN_VALUE;
        }

        @Override
        public BlockState tryApply(int x, int y, int z) {
            long pos = PosUtil.pack(x, z);
            if (this.lastPos != pos) {
                float noiseVal = this.noise.compute(x, z, 0);
                SurfaceRules.SurfaceRule newRule = null;
                for (Pair<Float, SurfaceRules.SurfaceRule> entry : this.rules) {
                    if (noiseVal > entry.getFirst()) {
                        newRule = entry.getSecond();
                        break;
                    }
                }
                this.lastPos = pos;
                this.rule = newRule;
            }
            return this.rule != null ? this.rule.tryApply(x, y, z) : null;
        }
    }
}
