package org.virgil698.NekoTerraForged.mixin.worldgen.feature.placement;

import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.MapCodec;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

import org.virgil698.NekoTerraForged.mixin.worldgen.feature.placement.poisson.FastPoissonModifier;
import org.virgil698.NekoTerraForged.mixin.worldgen.noise.module.Noise;
import org.virgil698.NekoTerraForged.mixin.worldgen.terrain.Terrain;

/**
 * RTF 放置修饰符注册
 * 移植自 ReTerraForged
 */
public class RTFPlacementModifiers {
    public static final String MOD_ID = "nekoterraforged";
    
    // 放置修饰符类型
    public static PlacementModifierType<TerrainFilter> TERRAIN_FILTER;
    public static PlacementModifierType<CellFilter> CELL_FILTER;
    public static PlacementModifierType<NoiseFilter> NOISE_FILTER;
    public static PlacementModifierType<DimensionFilter> DIMENSION_FILTER;
    public static PlacementModifierType<MacroBiomeFilter> MACRO_BIOME_FILTER;
    @SuppressWarnings("deprecation")
    public static PlacementModifierType<LegacyCountExtraModifier> LEGACY_COUNT_EXTRA;
    public static PlacementModifierType<FastPoissonModifier> FAST_POISSON;

    private static boolean registered = false;

    /**
     * 初始化并注册所有放置修饰符类型
     * 应在插件启动时调用
     */
    public static void bootstrap() {
        if (registered) {
            return;
        }
        registered = true;

        TERRAIN_FILTER = register("terrain_filter", TerrainFilter.CODEC);
        NOISE_FILTER = register("noise_filter", NoiseFilter.CODEC);
        DIMENSION_FILTER = register("dimension_filter", DimensionFilter.CODEC);
        MACRO_BIOME_FILTER = register("macro_biome_filter", MacroBiomeFilter.CODEC);
        LEGACY_COUNT_EXTRA = register("legacy_count_extra", LegacyCountExtraModifier.CODEC);
        FAST_POISSON = register("fast_poisson", FastPoissonModifier.CODEC);
    }

    /**
     * 注册放置修饰符类型
     */
    private static <P extends PlacementModifier> PlacementModifierType<P> register(String name, MapCodec<P> codec) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(MOD_ID, name);
        PlacementModifierType<P> type = () -> codec;
        return Registry.register(BuiltInRegistries.PLACEMENT_MODIFIER_TYPE, id, type);
    }

    // ==================== 工厂方法 ====================

    /**
     * 创建地形过滤器
     * @param exclude 是否排除指定地形
     * @param terrain 地形类型列表
     */
    public static TerrainFilter terrainFilter(boolean exclude, Terrain... terrain) {
        return new TerrainFilter(ImmutableSet.copyOf(terrain), exclude);
    }

    /**
     * 创建地形过滤器（包含模式）
     */
    public static TerrainFilter terrainFilterInclude(Terrain... terrain) {
        return terrainFilter(false, terrain);
    }

    /**
     * 创建地形过滤器（排除模式）
     */
    public static TerrainFilter terrainFilterExclude(Terrain... terrain) {
        return terrainFilter(true, terrain);
    }

    /**
     * 创建噪声过滤器
     * @param noise 噪声源
     * @param threshold 阈值
     */
    public static NoiseFilter noiseFilter(Noise noise, float threshold) {
        return new NoiseFilter(noise, threshold);
    }

    /**
     * 创建维度过滤器（黑名单模式）
     * @param blacklist 要排除的维度列表
     */
    @SafeVarargs
    public static DimensionFilter dimensionFilter(ResourceKey<LevelStem>... levels) {
        return new DimensionFilter(ImmutableList.copyOf(levels));
    }

    /**
     * 创建维度过滤器
     */
    public static DimensionFilter dimensionFilter(List<ResourceKey<LevelStem>> blacklist) {
        return new DimensionFilter(blacklist);
    }

    /**
     * 创建宏观生物群系过滤器
     * @param chance 放置概率 (0.0-1.0)
     */
    public static MacroBiomeFilter macroBiomeFilter(float chance) {
        return new MacroBiomeFilter(chance);
    }

    /**
     * 创建快速泊松分布修饰符
     * @param radius 最小间距半径
     * @param scale 缩放因子
     * @param biomeFade 生物群系边界淡化
     * @param densityVariationScale 密度变化缩放
     * @param densityVariation 密度变化量
     */
    public static FastPoissonModifier poisson(int radius, float scale, float biomeFade, int densityVariationScale, float densityVariation) {
        return poisson(radius, scale, 0.8F, biomeFade, densityVariationScale, densityVariation);
    }

    /**
     * 创建快速泊松分布修饰符（完整参数）
     * @param radius 最小间距半径
     * @param scale 缩放因子
     * @param jitter 抖动量
     * @param biomeFade 生物群系边界淡化
     * @param densityVariationScale 密度变化缩放
     * @param densityVariation 密度变化量
     */
    public static FastPoissonModifier poisson(int radius, float scale, float jitter, float biomeFade, int densityVariationScale, float densityVariation) {
        return new FastPoissonModifier(radius, scale, jitter, biomeFade, densityVariationScale, densityVariation);
    }

    /**
     * 创建旧版额外计数修饰符
     * @param count 基础数量
     * @param extraChance 额外数量的概率
     * @param extraCount 额外数量
     * @deprecated 使用 CountPlacement 代替
     */
    @Deprecated
    public static LegacyCountExtraModifier countExtra(int count, float extraChance, int extraCount) {
        return new LegacyCountExtraModifier(count, extraChance, extraCount);
    }

    /**
     * 获取资源位置
     */
    public static ResourceLocation id(String name) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, name);
    }
}
