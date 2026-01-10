package org.virgil698.NekoTerraForged.mixin.worldgen.surface.condition;

import net.minecraft.world.level.levelgen.SurfaceRules.Context;
import org.virgil698.NekoTerraForged.mixin.worldgen.cell.Cell;
import org.virgil698.NekoTerraForged.mixin.worldgen.noise.module.Noise;

/**
 * 阈值条件基类
 * 移植自 ReTerraForged
 */
public abstract class ThresholdCondition extends CellCondition {
    private Noise threshold;
    private Noise variance;

    public ThresholdCondition(Context context, Noise threshold, Noise variance) {
        super(context);
        this.threshold = threshold;
        this.variance = variance;
    }

    @Override
    public boolean test(Cell cell, int x, int z) {
        return this.sample(cell) + this.variance.compute(x, z, 0) > this.threshold.compute(x, z, 0);
    }

    protected abstract float sample(Cell cell);
}
