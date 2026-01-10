package org.virgil698.NekoTerraForged.mixin.worldgen.cell;

import org.virgil698.NekoTerraForged.mixin.worldgen.noise.module.Noise;

/**
 * 单元格填充器接口
 * 移植自 ReTerraForged
 */
@FunctionalInterface
public interface CellPopulator {
    /**
     * 应用填充器到单元格
     */
    void apply(Cell cell, float x, float z);

    /**
     * 映射噪声
     */
    default CellPopulator mapNoise(Noise.Visitor visitor) {
        return this;
    }
}
