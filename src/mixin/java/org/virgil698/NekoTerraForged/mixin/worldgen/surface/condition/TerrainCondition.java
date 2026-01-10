package org.virgil698.NekoTerraForged.mixin.worldgen.surface.condition;

import java.util.List;
import java.util.Set;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.SurfaceRules;
import net.minecraft.world.level.levelgen.SurfaceRules.Context;
import org.virgil698.NekoTerraForged.mixin.worldgen.cell.Cell;
import org.virgil698.NekoTerraForged.mixin.worldgen.terrain.Terrain;

/**
 * 地形条件
 * 移植自 ReTerraForged
 */
public class TerrainCondition extends CellCondition {
    private Set<Terrain> terrain;

    public TerrainCondition(Context context, Set<Terrain> terrain) {
        super(context);
        this.terrain = terrain;
    }

    @Override
    public boolean test(Cell cell, int x, int z) {
        return this.terrain.contains(cell.terrain);
    }

    public record Source(Set<Terrain> terrain) implements SurfaceRules.ConditionSource {
        public static final MapCodec<Source> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Terrain.CODEC.listOf().xmap(Set::copyOf, List::copyOf).fieldOf("terrain").forGetter(Source::terrain)
        ).apply(instance, Source::new));

        @Override
        public TerrainCondition apply(Context ctx) {
            return new TerrainCondition(ctx, this.terrain);
        }

        @Override
        public KeyDispatchDataCodec<Source> codec() {
            return KeyDispatchDataCodec.of(MAP_CODEC);
        }
    }
}
