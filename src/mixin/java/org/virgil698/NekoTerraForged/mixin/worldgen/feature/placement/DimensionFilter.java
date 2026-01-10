package org.virgil698.NekoTerraForged.mixin.worldgen.feature.placement;

import java.util.List;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementFilter;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

/**
 * 维度过滤器 - 根据维度黑名单过滤放置
 * 移植自 ReTerraForged
 */
public class DimensionFilter extends PlacementFilter {
    public static final MapCodec<DimensionFilter> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        ResourceKey.codec(Registries.LEVEL_STEM).listOf().fieldOf("blacklist").forGetter(filter -> filter.blacklist)
    ).apply(instance, DimensionFilter::new));

    private final List<ResourceKey<LevelStem>> blacklist;
    private final List<ResourceKey<Level>> levelKeys;

    public DimensionFilter(List<ResourceKey<LevelStem>> blacklist) {
        this.blacklist = blacklist;
        this.levelKeys = this.blacklist.stream().map(Registries::levelStemToLevel).toList();
    }

    @Override
    protected boolean shouldPlace(PlacementContext ctx, RandomSource rand, BlockPos pos) {
        WorldGenLevel level = ctx.getLevel();
        MinecraftServer server = level.getServer();

        if (server == null) return true;

        for (ResourceKey<Level> key : this.levelKeys) {
            if (server.getLevel(key) == level.getLevel()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public PlacementModifierType<DimensionFilter> type() {
        return RTFPlacementModifiers.DIMENSION_FILTER;
    }

    public List<ResourceKey<LevelStem>> getBlacklist() {
        return blacklist;
    }
}
