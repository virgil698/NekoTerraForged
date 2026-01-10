package org.virgil698.NekoTerraForged.mixin.worldgen.feature;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.DiskConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

import org.virgil698.NekoTerraForged.mixin.worldgen.feature.chance.ChanceFeature;

/**
 * RTF 特性注册
 * 移植自 ReTerraForged
 */
public class RTFFeatures {
    public static final String MOD_ID = "nekoterraforged";

    // 特性实例
    public static final Feature<BushFeature.Config> BUSH = new BushFeature(BushFeature.Config.CODEC);
    public static final Feature<DiskConfiguration> DISK = new DiskFeature(DiskConfiguration.CODEC);
    public static final Feature<ChanceFeature.Config> CHANCE = new ChanceFeature(ChanceFeature.Config.CODEC);
    public static final Feature<ErodeSnowFeature.Config> ERODE_SNOW = new ErodeSnowFeature(ErodeSnowFeature.Config.CODEC);
    public static final Feature<SwampSurfaceFeature.Config> SWAMP_SURFACE = new SwampSurfaceFeature(SwampSurfaceFeature.Config.CODEC);
    public static final Feature<ScreeFeature.Config> SCREE = new ScreeFeature(ScreeFeature.Config.CODEC);

    private static boolean registered = false;

    /**
     * 初始化并注册所有特性
     * 应在插件启动时调用
     */
    public static void bootstrap() {
        if (registered) {
            return;
        }
        registered = true;

        register("bush", BUSH);
        register("disk", DISK);
        register("chance", CHANCE);
        register("erode_snow", ERODE_SNOW);
        register("swamp_surface", SWAMP_SURFACE);
        register("scree", SCREE);
    }

    /**
     * 注册特性到 Minecraft 注册表
     */
    @SuppressWarnings("unchecked")
    private static <T extends FeatureConfiguration> Feature<T> register(String name, Feature<T> feature) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(MOD_ID, name);
        return (Feature<T>) Registry.register(
            (Registry<Feature<?>>) BuiltInRegistries.FEATURE,
            id,
            feature
        );
    }

    /**
     * 获取特性的资源位置
     */
    public static ResourceLocation id(String name) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, name);
    }
}
