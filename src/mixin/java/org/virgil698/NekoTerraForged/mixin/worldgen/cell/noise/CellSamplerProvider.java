package org.virgil698.NekoTerraForged.mixin.worldgen.cell.noise;

import org.jetbrains.annotations.Nullable;
import org.virgil698.NekoTerraForged.mixin.worldgen.cell.Cell;
import org.virgil698.NekoTerraForged.mixin.worldgen.cell.CellField;
import org.virgil698.NekoTerraForged.mixin.worldgen.noise.module.Noise;
import org.virgil698.NekoTerraForged.mixin.worldgen.noise.module.Noises;

/**
 * Cell 采样器提供者 - 用于在噪声计算中访问 Cell 字段
 * 移植自 ReTerraForged
 */
public class CellSamplerProvider implements Noise.Visitor {
    @Nullable
    private Cell cacheCell;

    public void setCacheCell(Cell cell) {
        this.cacheCell = cell;
    }

    public Cell getCacheCell() {
        return this.cacheCell;
    }

    @Override
    public Noise apply(Noise input) {
        if (input instanceof CellSampler sampler) {
            CellSampler newSampler = new CellSampler(sampler.field());
            newSampler.setProvider(this);
            return newSampler;
        }
        if (input instanceof Noises.CellNoise cellNoise) {
            CellSampler sampler = new CellSampler(cellNoise.field());
            sampler.setProvider(this);
            return sampler;
        }
        return input;
    }

    /**
     * Cell 采样器 - 从缓存的 Cell 中读取字段值
     */
    public static class CellSampler implements Noise {
        private final CellField field;
        @Nullable
        private CellSamplerProvider provider;

        public CellSampler(CellField field) {
            this.field = field;
        }

        public CellField field() {
            return field;
        }

        public void setProvider(CellSamplerProvider provider) {
            this.provider = provider;
        }

        @Override
        public float compute(float x, float z, int seed) {
            if (this.provider != null && this.provider.cacheCell != null) {
                return this.field.read(this.provider.cacheCell);
            }
            return 0.0F;
        }

        @Override
        public float minValue() {
            return 0.0F;
        }

        @Override
        public float maxValue() {
            return 1.0F;
        }

        @Override
        public Noise mapAll(Visitor visitor) {
            return visitor.apply(this);
        }
    }
}
