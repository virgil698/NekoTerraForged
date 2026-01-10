package org.virgil698.NekoTerraForged.mixin.worldgen.feature.placement;

import java.util.List;
import java.util.Set;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import org.virgil698.NekoTerraForged.mixin.worldgen.cell.Cell;
import org.virgil698.NekoTerraForged.mixin.worldgen.terrain.Terrain;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

/**
 * 地形过滤器
 * 基于地形类型过滤特征放置
 * 移植自 ReTerraForged
 */
public class TerrainFilter extends CellFilter {
    public static final MapCodec<TerrainFilter> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        Terrain.CODEC.listOf().xmap(Set::copyOf, List::copyOf).fieldOf("terrain").forGetter(filter -> filter.terrain),
        com.mojang.serialization.Codec.BOOL.fieldOf("exclude").forGetter(filter -> filter.exclude)
    ).apply(instance, TerrainFilter::new));

    private final Set<Terrain> terrain;
    private final boolean exclude;

    public TerrainFilter(Set<Terrain> terrain, boolean exclude) {
        this.terrain = terrain;
        this.exclude = exclude;
    }

    @Override
    protected boolean shouldPlace(Cell cell, PlacementContext ctx, RandomSource rand, BlockPos pos) {
        boolean match = this.terrain.contains(cell.terrain);
        return this.exclude ? !match : match;
    }

    @Override
    public PlacementModifierType<TerrainFilter> type() {
        return RTFPlacementModifiers.TERRAIN_FILTER;
    }

    public Set<Terrain> getTerrain() {
        return terrain;
    }

    public boolean isExclude() {
        return exclude;
    }
}
