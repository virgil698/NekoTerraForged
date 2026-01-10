package org.virgil698.NekoTerraForged.mixin.worldgen.feature.chance;

import java.util.function.Function;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

import org.virgil698.NekoTerraForged.mixin.registries.RTFBuiltInRegistries;

/**
 * 概率修改器接口
 * 用于根据地形条件调整特征放置概率
 * 移植自 ReTerraForged
 */
public interface ChanceModifier {
    /**
     * 概率修改器的分发编解码器
     * 通过注册表查找具体类型的编解码器
     */
    Codec<ChanceModifier> CODEC = RTFBuiltInRegistries.CHANCE_MODIFIER_TYPE.byNameCodec()
        .dispatch(ChanceModifier::codec, Function.identity());
    
    /**
     * 计算概率值
     * @param chanceCtx 概率上下文
     * @param placeCtx 放置上下文
     * @return 概率值 (0.0-1.0)
     */
    float getChance(ChanceContext chanceCtx, FeaturePlaceContext<?> placeCtx);
    
    /**
     * 获取此修改器的编解码器
     */
    MapCodec<? extends ChanceModifier> codec();
}
