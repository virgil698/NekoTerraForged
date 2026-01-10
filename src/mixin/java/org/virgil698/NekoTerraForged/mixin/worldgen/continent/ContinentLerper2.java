package org.virgil698.NekoTerraForged.mixin.worldgen.continent;

import org.virgil698.NekoTerraForged.mixin.worldgen.cell.Cell;
import org.virgil698.NekoTerraForged.mixin.worldgen.cell.CellPopulator;
import org.virgil698.NekoTerraForged.mixin.worldgen.noise.NoiseUtil;
import org.virgil698.NekoTerraForged.mixin.worldgen.noise.function.Interpolation;
import org.virgil698.NekoTerraForged.mixin.worldgen.noise.module.Noise;

/**
 * 大陆边缘插值器（2层）
 * 移植自 ReTerraForged
 */
public class ContinentLerper2 implements CellPopulator {
    private final CellPopulator lower;
    private final CellPopulator upper;
    private final Interpolation interpolation;
    private final float blendLower;
    private final float blendUpper;
    private final float blendRange;

    public ContinentLerper2(CellPopulator lower, CellPopulator upper, float min, float max) {
        this(lower, upper, min, max, Interpolation.LINEAR);
    }

    public ContinentLerper2(CellPopulator lower, CellPopulator upper, float min, float max, Interpolation interpolation) {
        this.lower = lower;
        this.upper = upper;
        this.interpolation = interpolation;
        this.blendLower = min;
        this.blendUpper = max;
        this.blendRange = this.blendUpper - this.blendLower;
    }

    @Override
    public void apply(Cell cell, float x, float y) {
        if (cell.continentEdge < this.blendLower) {
            this.lower.apply(cell, x, y);
            return;
        }
        if (cell.continentEdge > this.blendUpper) {
            this.upper.apply(cell, x, y);
            return;
        }
        float alpha = this.interpolation.apply((cell.continentEdge - this.blendLower) / this.blendRange);
        this.lower.apply(cell, x, y);
        float lowerHeight = cell.height;
        this.upper.apply(cell, x, y);
        float upperHeight = cell.height;
        cell.height = NoiseUtil.lerp(lowerHeight, upperHeight, alpha);
    }

    @Override
    public CellPopulator mapNoise(Noise.Visitor visitor) {
        return new ContinentLerper2(this.lower.mapNoise(visitor), this.upper.mapNoise(visitor), this.blendLower, this.blendUpper);
    }
}
