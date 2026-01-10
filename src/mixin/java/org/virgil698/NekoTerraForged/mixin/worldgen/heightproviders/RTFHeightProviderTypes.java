package org.virgil698.NekoTerraForged.mixin.worldgen.heightproviders;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.heightproviders.HeightProviderType;

/**
 * RTF高度提供器类型注册
 * 移植自 ReTerraForged
 */
public class RTFHeightProviderTypes {
    public static final HeightProviderType<LegacyCarverHeight> LEGACY_CARVER = register("legacy_carver", LegacyCarverHeight.CODEC);

    public static void bootstrap() {
    }

    private static <T extends HeightProvider> HeightProviderType<T> register(String name, MapCodec<T> codec) {
        HeightProviderType<T> type = () -> codec;
        Registry.register(BuiltInRegistries.HEIGHT_PROVIDER_TYPE, ResourceLocation.withDefaultNamespace(name), type);
        return type;
    }
}
