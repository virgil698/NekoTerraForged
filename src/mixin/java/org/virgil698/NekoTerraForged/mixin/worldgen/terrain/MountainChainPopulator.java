package org.virgil698.NekoTerraForged.mixin.worldgen.terrain;

import org.virgil698.NekoTerraForged.mixin.worldgen.cell.Cell;
import org.virgil698.NekoTerraForged.mixin.worldgen.cell.CellPopulator;
import org.virgil698.NekoTerraForged.mixin.worldgen.noise.NoiseUtil;
import org.virgil698.NekoTerraForged.mixin.worldgen.noise.function.Interpolation;
import org.virgil698.NekoTerraForged.mixin.worldgen.noise.module.Noise;

/**
 * 山脉链填充器
 * 移植自 ReTerraForged
 */
public class MountainChainPopulator implements CellPopulator {
    private final CellPopulator lower;
    private final CellPopulator upper;
    private final float blendLower;
    private final float blendUpper;
    private final float blendRange;

    public MountainChainPopulator(CellPopulator lower, CellPopulator upper, float min, float max) {
        this.lower = lower;
        this.upper = upper;
        this.blendLower = min;
        this.blendUpper = max;
        this.blendRange = this.blendUpper - this.blendLower;
    }

    @Override
    public void apply(Cell cell, float x, float y) {
        float select = cell.mountainChainAlpha;

        if (select < this.blendLower) {
            this.lower.apply(cell, x, y);
            return;
        }
        if (select > this.blendUpper) {
            this.upper.apply(cell, x, y);
            return;
        }
        float alpha = Interpolation.LINEAR.apply((select - this.blendLower) / this.blendRange);
        this.lower.apply(cell, x, y);
        float lowerHeight = cell.height;
        float lowerErosion = cell.erosion;
        float lowerWeirdness = cell.weirdness;
        this.upper.apply(cell, x, y);
        float upperHeight = cell.height;
        float upperErosion = cell.erosion;
        float upperWeirdness = cell.weirdness;
        cell.height = NoiseUtil.lerp(lowerHeight, upperHeight, alpha);
        cell.erosion = NoiseUtil.lerp(lowerErosion, upperErosion, alpha);
        cell.weirdness = NoiseUtil.lerp(lowerWeirdness, upperWeirdness, alpha);
    }

    @Override
    public CellPopulator mapNoise(Noise.Visitor visitor) {
        return new MountainChainPopulator(this.lower.mapNoise(visitor), this.upper.mapNoise(visitor), this.blendLower, this.blendUpper);
    }
}
