package org.virgil698.NekoTerraForged.mixin.worldgen.feature.chance;

import com.mojang.serialization.Codec;

import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

/**
 * 概率修改器接口
 * 移植自 ReTerraForged
 */
public interface ChanceModifier {
    // 简化版本，不使用注册表
    Codec<ChanceModifier> CODEC = Codec.unit(DefaultChanceModifier::new);
    
    float getChance(ChanceContext chanceCtx, FeaturePlaceContext<?> placeCtx);
    
    Codec<? extends ChanceModifier> codec();
    
    class DefaultChanceModifier implements ChanceModifier {
        @Override
        public float getChance(ChanceContext chanceCtx, FeaturePlaceContext<?> placeCtx) {
            return 1.0F;
        }
        
        @Override
        public Codec<? extends ChanceModifier> codec() {
            return CODEC;
        }
    }
}
