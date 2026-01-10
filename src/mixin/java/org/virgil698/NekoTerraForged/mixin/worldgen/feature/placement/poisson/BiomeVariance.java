package org.virgil698.NekoTerraForged.mixin.worldgen.feature.placement.poisson;

import org.virgil698.NekoTerraForged.mixin.worldgen.cell.Cell;
import org.virgil698.NekoTerraForged.mixin.worldgen.noise.module.Noise;
import org.virgil698.NekoTerraForged.mixin.worldgen.tile.Tile;

/**
 * 生物群系变差噪声
 * 用于在生物群系边界处调整特征放置密度
 * 移植自 ReTerraForged
 */
public class BiomeVariance implements Noise {
    public static final BiomeVariance NONE = new BiomeVariance(null, 0.0F) {
        @Override
        public float compute(float x, float y, int seed) {
            return NO_SPREAD;
        }
    };

    public static final float MIN_FADE = 0.025F;
    public static final float NO_SPREAD = 1F;
    private static final float SPREAD_VARIANCE = 1F;
    private static final float MAX_SPREAD = NO_SPREAD + SPREAD_VARIANCE;

    private final float fade;
    private final float range;
    private final Tile.Chunk chunk;

    public BiomeVariance(Tile.Chunk chunk, float fade) {
        this.chunk = chunk;
        this.fade = fade;
        this.range = fade - MIN_FADE;
    }

    @Override
    public float compute(float x, float z, int seed) {
        Cell cell = this.chunk.getCell((int) x, (int) z);
        float edge = 0.02F + cell.biomeRegionEdge;

        if (edge >= this.fade) {
            return NO_SPREAD;
        }

        if (edge <= MIN_FADE) {
            return MAX_SPREAD;
        }

        float alpha = (edge - MIN_FADE) / this.range;
        alpha = 1 - alpha;
        return NO_SPREAD + alpha * SPREAD_VARIANCE;
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
