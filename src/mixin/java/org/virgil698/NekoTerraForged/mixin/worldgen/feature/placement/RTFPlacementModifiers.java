package org.virgil698.NekoTerraForged.mixin.worldgen.feature.placement;

import java.util.List;
import java.util.Set;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

import org.virgil698.NekoTerraForged.mixin.worldgen.feature.placement.poisson.FastPoissonModifier;
import org.virgil698.NekoTerraForged.mixin.worldgen.noise.module.Noise;
import org.virgil698.NekoTerraForged.mixin.worldgen.terrain.Terrain;

/**
 * RTF 放置修饰符注册
 * 移植自 ReTerraForged
 */
public class RTFPlacementModifiers {
    public static PlacementModifierType<TerrainFilter> TERRAIN_FILTER;
    public static PlacementModifierType<CellFilter> CELL_FILTER;
    public static PlacementModifierType<NoiseFilter> NOISE_FILTER;
    public static PlacementModifierType<DimensionFilter> DIMENSION_FILTER;
    public static PlacementModifierType<MacroBiomeFilter> MACRO_BIOME_FILTER;
    public static PlacementModifierType<LegacyCountExtraModifier> LEGACY_COUNT_EXTRA;
    public static PlacementModifierType<FastPoissonModifier> FAST_POISSON;

    public static void bootstrap() {
        // 在插件初始化时注册放置修饰符类型
        // 由于 Leaves 插件不能直接注册到 BuiltInRegistries，
        // 这里只是占位，实际使用时需要通过数据包或其他方式
    }

    public static TerrainFilter terrainFilter(boolean exclude, Terrain... terrain) {
        return new TerrainFilter(Set.of(terrain), exclude);
    }
    
    public static NoiseFilter noiseFilter(Noise noise, float threshold) {
        return new NoiseFilter(noise, threshold);
    }
    
    public static DimensionFilter dimensionFilter(List<ResourceKey<LevelStem>> blacklist) {
        return new DimensionFilter(blacklist);
    }
    
    public static MacroBiomeFilter macroBiomeFilter(float chance) {
        return new MacroBiomeFilter(chance);
    }
}
