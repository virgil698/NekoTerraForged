package org.virgil698.NekoTerraForged.mixin.worldgen.biome.modifier;

import java.util.function.Function;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

import org.virgil698.NekoTerraForged.mixin.registries.RTFBuiltInRegistries;

/**
 * 生物群系修改器接口
 * 用于在运行时修改生物群系的特征配置
 * 移植自 ReTerraForged
 */
public interface BiomeModifier {
    /**
     * 生物群系修改器的分发编解码器
     * 通过注册表查找具体类型的编解码器
     */
    Codec<BiomeModifier> CODEC = RTFBuiltInRegistries.BIOME_MODIFIER_TYPE.byNameCodec()
        .dispatch(BiomeModifier::codec, Function.identity());
    
    /**
     * 获取此修改器的编解码器
     */
    MapCodec<? extends BiomeModifier> codec();
}
