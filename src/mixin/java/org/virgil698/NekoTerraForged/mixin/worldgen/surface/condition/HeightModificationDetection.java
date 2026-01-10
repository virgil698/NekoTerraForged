package org.virgil698.NekoTerraForged.mixin.worldgen.surface.condition;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.SurfaceRules;
import net.minecraft.world.level.levelgen.SurfaceRules.Context;

import org.virgil698.NekoTerraForged.mixin.worldgen.GeneratorContext;
import org.virgil698.NekoTerraForged.mixin.worldgen.cell.Cell;

/**
 * 高度修改检测条件
 * 用于检测结构等对高度的修改
 * 移植自 ReTerraForged
 */
public class HeightModificationDetection extends CellCondition {
    private Target target;

    private HeightModificationDetection(Context context, Target target) {
        super(context);
        this.target = target;
    }

    @Override
    public boolean test(Cell cell, int blockX, int blockZ) {
        return this.target.test(cell, blockX, blockZ, this.context, this.generatorContext);
    }

    public record Source(Target target) implements SurfaceRules.ConditionSource {
        public static final MapCodec<Source> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Target.CODEC.fieldOf("target").forGetter(Source::target)
        ).apply(instance, Source::new));

        @Override
        public HeightModificationDetection apply(Context ctx) {
            return new HeightModificationDetection(ctx, this.target);
        }

        @Override
        public KeyDispatchDataCodec<Source> codec() {
            return KeyDispatchDataCodec.of(MAP_CODEC);
        }
    }


    public enum Target implements StringRepresentable {
        STRUCTURE_BEARDIFIER("structure_beardifier") {
            @Override
            public boolean test(Cell cell, int blockX, int blockZ, Context surfaceContext, GeneratorContext generatorContext) {
                if (generatorContext == null) {
                    return false;
                }
                int worldSurfaceY = surfaceContext.chunk.getHeight(Heightmap.Types.WORLD_SURFACE_WG, blockX & 0xF, blockZ & 0xF);
                int expectedY = generatorContext.levels.scale(cell.height);
                return surfaceContext.blockY == worldSurfaceY && surfaceContext.blockY != expectedY;
            }
        };

        public static final Codec<Target> CODEC = StringRepresentable.fromEnum(Target::values);

        private String name;

        private Target(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }

        public abstract boolean test(Cell cell, int blockX, int blockZ, SurfaceRules.Context surfaceContext, GeneratorContext generatorContext);
    }
}
