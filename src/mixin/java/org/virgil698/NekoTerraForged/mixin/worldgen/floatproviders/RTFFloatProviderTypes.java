package org.virgil698.NekoTerraForged.mixin.worldgen.floatproviders;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.valueproviders.FloatProvider;
import net.minecraft.util.valueproviders.FloatProviderType;

/**
 * RTF浮点数提供器类型注册
 * 移植自 ReTerraForged
 */
public class RTFFloatProviderTypes {
    public static final FloatProviderType<LegacyCanyonYScale> LEGACY_CANYON_Y_SCALE = register("legacy_canyon_y_scale", LegacyCanyonYScale.CODEC);

    public static void bootstrap() {
    }

    private static <T extends FloatProvider> FloatProviderType<T> register(String name, MapCodec<T> codec) {
        FloatProviderType<T> type = () -> codec;
        Registry.register(BuiltInRegistries.FLOAT_PROVIDER_TYPE, ResourceLocation.withDefaultNamespace(name), type);
        return type;
    }
}
