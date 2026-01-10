package org.virgil698.NekoTerraForged.mixin.worldgen.terrain.region;

import org.virgil698.NekoTerraForged.mixin.worldgen.cell.Cell;
import org.virgil698.NekoTerraForged.mixin.worldgen.cell.CellPopulator;
import org.virgil698.NekoTerraForged.mixin.worldgen.heightmap.RegionConfig;
import org.virgil698.NekoTerraForged.mixin.worldgen.noise.NoiseUtil;
import org.virgil698.NekoTerraForged.mixin.worldgen.noise.NoiseUtil.Vec2f;
import org.virgil698.NekoTerraForged.mixin.worldgen.noise.domain.Domain;
import org.virgil698.NekoTerraForged.mixin.worldgen.noise.domain.Domains;
import org.virgil698.NekoTerraForged.mixin.worldgen.noise.function.DistanceFunction;
import org.virgil698.NekoTerraForged.mixin.worldgen.noise.function.EdgeFunction;
import org.virgil698.NekoTerraForged.mixin.worldgen.noise.module.Noise;
import org.virgil698.NekoTerraForged.mixin.worldgen.noise.module.Noises;
import org.virgil698.NekoTerraForged.mixin.worldgen.util.PosUtil;

/**
 * 区域模块 - 负责将世界划分为不同的地形区域
 * 使用 Worley 噪声算法生成 Voronoi 单元格，每个单元格代表一个地形区域
 * 移植自 ReTerraForged
 */
public class RegionModule implements CellPopulator {
    private final int seed;
    private final float frequency;
    private final float edgeMin;
    private final float edgeMax;
    private final float edgeRange;
    private final Domain warp;

    public RegionModule(RegionConfig regionConfig) {
        this.seed = regionConfig.seed() + 7;
        this.edgeMin = 0.0F;
        this.edgeMax = 0.5F;
        this.edgeRange = this.edgeMax - this.edgeMin;
        this.frequency = 1.0F / regionConfig.scale();
        this.warp = Domains.domain(regionConfig.warpX(), regionConfig.warpZ(), Noises.constant(regionConfig.warpStrength()));
    }

    private RegionModule(int seed, float frequency, float edgeMin, float edgeMax, float edgeRange, Domain warp) {
        this.seed = seed;
        this.frequency = frequency;
        this.edgeMin = edgeMin;
        this.edgeMax = edgeMax;
        this.edgeRange = edgeRange;
        this.warp = warp;
    }

    @Override
    public void apply(Cell cell, float x, float z) {
        float ox = this.warp.getOffsetX(x, z, 0);
        float oz = this.warp.getOffsetZ(x, z, 0);
        float px = x + ox;
        float py = z + oz;
        px *= this.frequency;
        py *= this.frequency;
        int cellX = 0;
        int cellY = 0;
        float centerX = 0.0F;
        float centerY = 0.0F;
        int xi = NoiseUtil.floor(px);
        int yi = NoiseUtil.floor(py);
        float edgeDistance = Float.MAX_VALUE;
        float edgeDistance2 = Float.MAX_VALUE;
        DistanceFunction dist = DistanceFunction.NATURAL;
        for (int dy = -1; dy <= 1; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                int cx = xi + dx;
                int cy = yi + dy;
                Vec2f vec = NoiseUtil.cell(this.seed, cx, cy);
                float vecX = cx + vec.x() * 0.7F;
                float vecY = cy + vec.y() * 0.7F;
                float distance = dist.apply(vecX - px, vecY - py);
                if (distance < edgeDistance) {
                    edgeDistance2 = edgeDistance;
                    edgeDistance = distance;
                    centerX = vecX;
                    centerY = vecY;
                    cellX = cx;
                    cellY = cy;
                } else if (distance < edgeDistance2) {
                    edgeDistance2 = distance;
                }
            }
        }
        cell.terrainRegionId = cellValue(this.seed, cellX, cellY);
        cell.terrainRegionEdge = this.edgeValue(edgeDistance, edgeDistance2);
        cell.terrainMask *= this.maskValue(edgeDistance, edgeDistance2);
        cell.terrainRegionCenter = PosUtil.pack(centerX / this.frequency, centerY / this.frequency);
    }

    private float edgeValue(float distance, float distance2) {
        EdgeFunction edge = EdgeFunction.DISTANCE_2_DIV;
        float value = edge.apply(distance, distance2);
        float edgeValue = 1.0F - NoiseUtil.map(value, edge.min(), edge.max(), edge.range());
        edgeValue = NoiseUtil.pow(edgeValue, 1.5F);
        if (edgeValue < this.edgeMin) {
            return 0.0F;
        }
        if (edgeValue > this.edgeMax) {
            return 1.0F;
        }
        return (edgeValue - this.edgeMin) / this.edgeRange;
    }

    private float maskValue(float distance, float distance2) {
        EdgeFunction edge = EdgeFunction.DISTANCE_2_DIV;
        float value = edge.apply(distance, distance2);
        float edgeValue = 1.0F - NoiseUtil.map(value, edge.min(), edge.max(), edge.range());
        edgeValue = NoiseUtil.map(edgeValue, 0.5F, 0.9F);
        return NoiseUtil.pow(edgeValue, 4.0F + 5.0F * edgeValue);
    }

    private static float cellValue(int seed, int cellX, int cellY) {
        float value = NoiseUtil.valCoord2D(seed, cellX, cellY);
        return NoiseUtil.map(value, -1.0F, 1.0F, 2.0F);
    }

    @Override
    public CellPopulator mapNoise(Noise.Visitor visitor) {
        return new RegionModule(this.seed, this.frequency, this.edgeMin, this.edgeMax, this.edgeRange, this.warp.mapAll(visitor));
    }
}
