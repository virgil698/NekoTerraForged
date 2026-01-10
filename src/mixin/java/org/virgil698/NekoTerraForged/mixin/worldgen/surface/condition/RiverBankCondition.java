package org.virgil698.NekoTerraForged.mixin.worldgen.surface.condition;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.SurfaceRules;
import net.minecraft.world.level.levelgen.SurfaceRules.Context;
import org.virgil698.NekoTerraForged.mixin.worldgen.cell.Cell;
import org.virgil698.NekoTerraForged.mixin.worldgen.noise.module.Noise;

/**
 * 河岸条件
 * 移植自 ReTerraForged
 */
public class RiverBankCondition extends ThresholdCondition {

    public RiverBankCondition(Context context, Noise threshold, Noise variance) {
        super(context, threshold, variance);
    }

    @Override
    protected float sample(Cell cell) {
        return cell.riverDistance;
    }

    public record Source(Noise threshold, Noise variance) implements SurfaceRules.ConditionSource {
        public static final MapCodec<Source> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Noise.DIRECT_CODEC.fieldOf("threshold").forGetter(Source::threshold),
            Noise.DIRECT_CODEC.fieldOf("variance").forGetter(Source::variance)
        ).apply(instance, Source::new));

        @Override
        public RiverBankCondition apply(Context ctx) {
            return new RiverBankCondition(ctx, this.threshold, this.variance);
        }

        @Override
        public KeyDispatchDataCodec<Source> codec() {
            return KeyDispatchDataCodec.of(MAP_CODEC);
        }
    }
}
