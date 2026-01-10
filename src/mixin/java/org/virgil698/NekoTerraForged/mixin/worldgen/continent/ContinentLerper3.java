package org.virgil698.NekoTerraForged.mixin.worldgen.continent;

import org.virgil698.NekoTerraForged.mixin.worldgen.cell.Cell;
import org.virgil698.NekoTerraForged.mixin.worldgen.cell.CellPopulator;
import org.virgil698.NekoTerraForged.mixin.worldgen.noise.NoiseUtil;
import org.virgil698.NekoTerraForged.mixin.worldgen.noise.function.Interpolation;
import org.virgil698.NekoTerraForged.mixin.worldgen.noise.module.Noise;

/**
 * 大陆边缘插值器（3层）
 * 移植自 ReTerraForged
 */
public class ContinentLerper3 implements CellPopulator {
    private final CellPopulator lower;
    private final CellPopulator middle;
    private final CellPopulator upper;
    private final Interpolation interpolation;
    private final float midpoint;
    private final float blendLower;
    private final float blendUpper;
    private final float lowerRange;
    private final float upperRange;

    public ContinentLerper3(CellPopulator lower, CellPopulator middle, CellPopulator upper, float min, float mid, float max) {
        this(lower, middle, upper, min, mid, max, Interpolation.CURVE3);
    }

    public ContinentLerper3(CellPopulator lower, CellPopulator middle, CellPopulator upper, float min, float mid, float max, Interpolation interpolation) {
        this.lower = lower;
        this.upper = upper;
        this.middle = middle;
        this.interpolation = interpolation;
        this.midpoint = mid;
        this.blendLower = min;
        this.blendUpper = max;
        this.lowerRange = this.midpoint - this.blendLower;
        this.upperRange = this.blendUpper - this.midpoint;
    }

    @Override
    public void apply(Cell cell, float x, float y) {
        float select = cell.continentEdge;
        if (select < this.blendLower) {
            this.lower.apply(cell, x, y);
            return;
        }
        if (select > this.blendUpper) {
            this.upper.apply(cell, x, y);
            return;
        }
        if (select < this.midpoint) {
            float alpha = this.interpolation.apply((select - this.blendLower) / this.lowerRange);
            this.lower.apply(cell, x, y);
            float lowerHeight = cell.height;
            this.middle.apply(cell, x, y);
            cell.height = NoiseUtil.lerp(lowerHeight, cell.height, alpha);
        } else {
            float alpha = this.interpolation.apply((select - this.midpoint) / this.upperRange);
            this.middle.apply(cell, x, y);
            float lowerHeight = cell.height;
            this.upper.apply(cell, x, y);
            cell.height = NoiseUtil.lerp(lowerHeight, cell.height, alpha);
        }
    }

    @Override
    public CellPopulator mapNoise(Noise.Visitor visitor) {
        return new ContinentLerper3(this.lower.mapNoise(visitor), this.middle.mapNoise(visitor), this.upper.mapNoise(visitor), this.blendLower, this.midpoint, this.blendUpper, this.interpolation);
    }
}
