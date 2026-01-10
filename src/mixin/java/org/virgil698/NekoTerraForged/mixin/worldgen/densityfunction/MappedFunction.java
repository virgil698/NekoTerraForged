package org.virgil698.NekoTerraForged.mixin.worldgen.densityfunction;

import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.DensityFunction;

/**
 * 映射函数接口
 * 移植自 ReTerraForged
 */
public interface MappedFunction extends DensityFunction.SimpleFunction {

    @Override
    default KeyDispatchDataCodec<? extends Marker> codec() {
        throw new UnsupportedOperationException();
    }

    public interface Marker extends DensityFunction.SimpleFunction {

        @Override
        default double compute(FunctionContext ctx) {
            throw new UnsupportedOperationException();
        }

        @Override
        default double minValue() {
            return Double.NEGATIVE_INFINITY;
        }

        @Override
        default double maxValue() {
            return Double.POSITIVE_INFINITY;
        }
    }
}
