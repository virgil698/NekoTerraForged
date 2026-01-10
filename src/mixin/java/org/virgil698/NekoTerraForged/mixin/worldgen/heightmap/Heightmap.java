package org.virgil698.NekoTerraForged.mixin.worldgen.heightmap;

import org.virgil698.NekoTerraForged.mixin.worldgen.GeneratorContext;
import org.virgil698.NekoTerraForged.mixin.worldgen.cell.Cell;
import org.virgil698.NekoTerraForged.mixin.worldgen.climate.Climate;
import org.virgil698.NekoTerraForged.mixin.worldgen.continent.Continent;
import org.virgil698.NekoTerraForged.mixin.worldgen.continent.ContinentType;
import org.virgil698.NekoTerraForged.mixin.worldgen.noise.NoiseUtil;
import org.virgil698.NekoTerraForged.mixin.worldgen.noise.module.Noise;
import org.virgil698.NekoTerraForged.mixin.worldgen.noise.module.Noises;
import org.virgil698.NekoTerraForged.mixin.worldgen.rivermap.Rivermap;
import org.virgil698.NekoTerraForged.mixin.worldgen.terrain.TerrainType;

/**
 * 高度图生成器
 * 移植自 ReTerraForged
 */
public class Heightmap {
    private final Continent continent;
    private final Climate climate;
    private final Levels levels;
    private final float terrainFrequency;
    private final Noise mountainChainAlpha;
    private final Noise baseNoise;
    private final Noise beachAlpha;

    // 控制点
    private final float deepOcean;
    private final float shallowOcean;
    private final float coast;
    private final float nearInland;
    private final float midInland;
    private final float farInland;

    public Heightmap(GeneratorContext ctx) {
        this.levels = ctx.levels;
        this.terrainFrequency = 1.0F / 300.0F;

        // 创建大陆
        this.continent = ContinentType.MULTI.create(ctx.seed, ctx);

        // 创建气候系统
        this.climate = Climate.make(this.continent, ctx);

        // 创建山脉链噪声
        this.mountainChainAlpha = Noises.warpPerlin(
            Noises.worleyEdge(ctx.seed.next(), 1000),
            ctx.seed.next(), 333, 2, 250.0F
        );

        // 基础地形噪声
        this.baseNoise = Noises.simplex(ctx.seed.next(), 200, 4);

        // 海滩噪声
        Noise beachNoise = Noises.perlin(ctx.seed.next(), 20, 1);
        this.beachAlpha = Noises.mul(beachNoise, ctx.levels.scale(5));

        // 控制点
        this.deepOcean = 0.1F;
        this.shallowOcean = 0.25F;
        this.coast = 0.35F;
        this.nearInland = 0.45F;
        this.midInland = 0.6F;
        this.farInland = 0.8F;
    }

    public Continent continent() {
        return continent;
    }

    public Climate climate() {
        return climate;
    }

    public Levels levels() {
        return levels;
    }

    public void applyContinent(Cell cell, float x, float z) {
        this.continent.apply(cell, x, z);
    }

    public void applyTerrain(Cell cell, float x, float z) {
        Rivermap rivermap = continent.getRivermap(cell);
        applyTerrain(cell, x, z, rivermap);
    }

    public void applyTerrain(Cell cell, float x, float z, Rivermap rivermap) {
        cell.terrain = TerrainType.PLAINS;
        cell.riverDistance = 1.0F;
        cell.mountainChainAlpha = this.mountainChainAlpha.compute(x, z, 0);

        rivermap.apply(cell, x, z);

        float mountainMask = NoiseUtil.map(cell.mountainChainAlpha, 0.45F, 0.65F);
        cell.terrainMask = Math.min(cell.terrainMask + mountainMask, 1.0F);

        // 计算基础高度
        float continentNoise = cell.continentNoise;
        float baseHeight;

        if (continentNoise <= deepOcean) {
            // 深海
            cell.terrain = TerrainType.DEEP_OCEAN;
            baseHeight = levels.water(-30);
        } else if (continentNoise <= shallowOcean) {
            // 浅海
            cell.terrain = TerrainType.OCEAN;
            baseHeight = levels.water(-10);
        } else if (continentNoise <= coast) {
            // 海岸
            cell.terrain = TerrainType.COAST;
            float alpha = NoiseUtil.map(continentNoise, shallowOcean, coast);
            baseHeight = NoiseUtil.lerp(levels.water(-5), levels.water(3), alpha);
        } else {
            // 陆地
            float terrainNoise = baseNoise.compute(x * terrainFrequency, z * terrainFrequency, 0);
            float alpha = NoiseUtil.map(continentNoise, coast, farInland);
            baseHeight = NoiseUtil.lerp(levels.ground(0), levels.ground(50), alpha);
            baseHeight += terrainNoise * levels.scale(30);

            // 根据高度设置地形类型
            if (baseHeight > levels.ground(40)) {
                cell.terrain = TerrainType.MOUNTAINS;
            } else if (baseHeight > levels.ground(20)) {
                cell.terrain = TerrainType.HILLS;
            }
        }

        cell.height = baseHeight;
    }

    public void applyClimate(Cell cell, float x, float z) {
        cell.weirdness = -cell.weirdness;
        this.climate.apply(cell, x, z);
    }

    public void applyPost(Cell cell, float x, float z) {
        float continentNoise = cell.continentNoise;

        // 计算大陆性
        if (continentNoise <= deepOcean || cell.terrain.isDeepOcean()) {
            float alpha = NoiseUtil.map(continentNoise, 0.0F, deepOcean);
            cell.continentalness = NoiseUtil.lerp(-1.05F, -0.455F, alpha);
        } else if (continentNoise <= shallowOcean || cell.terrain.isShallowOcean()) {
            float alpha = NoiseUtil.map(continentNoise, deepOcean, shallowOcean);
            cell.continentalness = NoiseUtil.lerp(-0.455F, -0.19F, alpha);
        } else if (continentNoise <= nearInland) {
            float alpha = NoiseUtil.map(continentNoise, shallowOcean, nearInland);
            cell.continentalness = NoiseUtil.lerp(-0.11F, 0.03F, alpha);
        } else if (continentNoise <= midInland) {
            float alpha = NoiseUtil.map(continentNoise, nearInland, midInland);
            cell.continentalness = NoiseUtil.lerp(0.03F, 0.3F, alpha);
        } else {
            float alpha = NoiseUtil.map(continentNoise, midInland, farInland);
            alpha = Math.min(alpha, 1.0F);
            cell.continentalness = NoiseUtil.lerp(0.3F, 1.0F, alpha);
        }

        // 海滩处理
        if (cell.terrain.isCoast() && cell.height + this.beachAlpha.compute(x, z, 0) < this.levels.water(5)) {
            float alpha = NoiseUtil.clamp(cell.continentEdge, shallowOcean, coast);
            alpha = NoiseUtil.lerp(alpha, shallowOcean, coast, 0.0F, 1.0F);
            cell.continentalness = NoiseUtil.lerp(-0.19F, -0.11F, alpha);
        }
    }

    /**
     * 创建此高度图的缓存副本
     */
    public Heightmap cache() {
        // 返回自身，因为Heightmap是无状态的
        return this;
    }

    public static Heightmap make(GeneratorContext ctx) {
        return new Heightmap(ctx);
    }
}
