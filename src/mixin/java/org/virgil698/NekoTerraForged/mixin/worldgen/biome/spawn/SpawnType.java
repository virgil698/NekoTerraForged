package org.virgil698.NekoTerraForged.mixin.worldgen.biome.spawn;

import java.util.List;

import org.virgil698.NekoTerraForged.mixin.worldgen.GeneratorContext;
import org.virgil698.NekoTerraForged.mixin.worldgen.util.PosUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.Climate.ParameterPoint;

/**
 * 出生点类型枚举
 * 移植自 ReTerraForged
 */
public enum SpawnType {
    CONTINENT_CENTER("CONTINENT_CENTER") {
        private static final Climate.Parameter FULL_RANGE = Climate.Parameter.span(-1.0F, 1.0F);
        private static final Climate.Parameter SURFACE_DEPTH = Climate.Parameter.point(0.0F);
        private static final Climate.Parameter INLAND_CONTINENTALNESS = Climate.Parameter.span(-0.11F, 0.55F);

        @Override
        public BlockPos getSearchCenter(GeneratorContext ctx) {
            if (ctx.localHeightmap != null) {
                long center = ctx.localHeightmap.get().continent().getNearestCenter(0.0F, 0.0F);
                return new BlockPos(PosUtil.unpackLeft(center), 0, PosUtil.unpackRight(center));
            }
            return BlockPos.ZERO;
        }

        @Override
        public List<ParameterPoint> getParameterPoints() {
            return List.of(
                new Climate.ParameterPoint(FULL_RANGE, FULL_RANGE, 
                    Climate.Parameter.span(INLAND_CONTINENTALNESS, FULL_RANGE), 
                    FULL_RANGE, SURFACE_DEPTH, FULL_RANGE, 0L),
                new Climate.ParameterPoint(FULL_RANGE, FULL_RANGE, 
                    Climate.Parameter.span(INLAND_CONTINENTALNESS, FULL_RANGE), 
                    FULL_RANGE, SURFACE_DEPTH, FULL_RANGE, 0L)
            );
        }
    },
    ISLANDS("ISLANDS") {
        @Override
        public BlockPos getSearchCenter(GeneratorContext ctx) {
            return BlockPos.ZERO;
        }

        @Override
        public List<ParameterPoint> getParameterPoints() {
            return List.of();
        }
    },
    WORLD_ORIGIN("WORLD_ORIGIN") {
        @Override
        public BlockPos getSearchCenter(GeneratorContext ctx) {
            return BlockPos.ZERO;
        }

        @Override
        public List<ParameterPoint> getParameterPoints() {
            return List.of();
        }
    };

    private final String name;

    SpawnType(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public abstract BlockPos getSearchCenter(GeneratorContext ctx);

    public abstract List<ParameterPoint> getParameterPoints();
}
