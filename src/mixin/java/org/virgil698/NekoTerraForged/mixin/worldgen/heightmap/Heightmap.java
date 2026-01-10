package org.virgil698.NekoTerraForged.mixin.worldgen.heightmap;

import org.virgil698.NekoTerraForged.mixin.worldgen.GeneratorContext;
import org.virgil698.NekoTerraForged.mixin.worldgen.biome.Continentalness;
import org.virgil698.NekoTerraForged.mixin.worldgen.cell.Cell;
import org.virgil698.NekoTerraForged.mixin.worldgen.cell.CellField;
import org.virgil698.NekoTerraForged.mixin.worldgen.cell.CellPopulator;
import org.virgil698.NekoTerraForged.mixin.worldgen.cell.noise.CellSamplerProvider;
import org.virgil698.NekoTerraForged.mixin.worldgen.climate.Climate;
import org.virgil698.NekoTerraForged.mixin.worldgen.continent.Continent;
import org.virgil698.NekoTerraForged.mixin.worldgen.continent.ContinentLerper2;
import org.virgil698.NekoTerraForged.mixin.worldgen.continent.ContinentLerper3;
import org.virgil698.NekoTerraForged.mixin.worldgen.noise.NoiseUtil;
import org.virgil698.NekoTerraForged.mixin.worldgen.noise.function.DistanceFunction;
import org.virgil698.NekoTerraForged.mixin.worldgen.noise.function.EdgeFunction;
import org.virgil698.NekoTerraForged.mixin.worldgen.noise.function.Interpolation;
import org.virgil698.NekoTerraForged.mixin.worldgen.noise.module.Cache2d;
import org.virgil698.NekoTerraForged.mixin.worldgen.noise.module.Noise;
import org.virgil698.NekoTerraForged.mixin.worldgen.noise.module.Noises;
import org.virgil698.NekoTerraForged.mixin.worldgen.rivermap.Rivermap;
import org.virgil698.NekoTerraForged.mixin.worldgen.terrain.MountainChainPopulator;
import org.virgil698.NekoTerraForged.mixin.worldgen.terrain.TerrainCategory;
import org.virgil698.NekoTerraForged.mixin.worldgen.terrain.TerrainProvider;
import org.virgil698.NekoTerraForged.mixin.worldgen.terrain.TerrainType;
import org.virgil698.NekoTerraForged.mixin.worldgen.terrain.populator.Populators;
import org.virgil698.NekoTerraForged.mixin.worldgen.terrain.populator.VolcanoPopulator;
import org.virgil698.NekoTerraForged.mixin.worldgen.terrain.region.RegionLerper;
import org.virgil698.NekoTerraForged.mixin.worldgen.terrain.region.RegionModule;
import org.virgil698.NekoTerraForged.mixin.worldgen.terrain.region.RegionSelector;
import org.virgil698.NekoTerraForged.mixin.worldgen.util.Seed;

/**
 * 高度图生成器 - 核心地形生成类
 * 负责协调大陆、地形区域、气候等系统生成完整的地形
 * 移植自 ReTerraForged
 */
public record Heightmap(
    CellSamplerProvider cellProvider,
    CellPopulator terrain,
    CellPopulator region,
    Continent continent,
    Climate climate,
    Levels levels,
    ControlPoints controlPoints,
    float terrainFrequency,
    Noise mountainChainAlpha,
    Noise beachAlpha
) {

    /**
     * 创建此高度图的缓存副本
     */
    public Heightmap cache() {
        CellSamplerProvider newCellProvider = new CellSamplerProvider();
        return new Heightmap(
            newCellProvider,
            this.terrain.mapNoise((noise) -> {
                if (noise instanceof Cache2d cache2d) {
                    return new Cache2d.Cached(cache2d.noise());
                }
                return newCellProvider.apply(noise);
            }),
            this.region,
            this.continent,
            this.climate,
            this.levels,
            this.controlPoints,
            this.terrainFrequency,
            this.mountainChainAlpha,
            this.beachAlpha
        );
    }

    /**
     * 应用大陆生成
     */
    public void applyContinent(Cell cell, float x, float z) {
        this.continent.apply(cell, x, z);
    }

    /**
     * 应用地形生成 (无 Rivermap 版本)
     */
    public void applyTerrain(Cell cell, float x, float z) {
        Rivermap rivermap = this.continent.getRivermap(cell);
        applyTerrain(cell, x, z, rivermap);
    }

    /**
     * 应用地形生成
     */
    public void applyTerrain(Cell cell, float x, float z, Rivermap rivermap) {
        cell.terrain = TerrainType.PLAINS;
        cell.riverDistance = 1.0F;
        cell.mountainChainAlpha = this.mountainChainAlpha.compute(x, z, 0);

        rivermap.apply(cell, x, z);
        this.region.apply(cell, x, z);

        float mountainMask = NoiseUtil.map(cell.mountainChainAlpha, 0.45F, 0.65F);
        cell.terrainMask = Math.min(cell.terrainMask + mountainMask, 1.0F);

        this.terrain.apply(cell, x * this.terrainFrequency, z * this.terrainFrequency);

        VolcanoPopulator.modifyVolcanoType(cell, this.levels);
    }

    /**
     * 应用气候生成
     */
    public void applyClimate(Cell cell, float x, float z) {
        cell.weirdness = -cell.weirdness;
        this.climate.apply(cell, x, z);
    }

    /**
     * 应用后处理
     */
    public void applyPost(Cell cell, float x, float z) {
        float deepOcean = this.controlPoints.deepOcean();
        float shallowOcean = this.controlPoints.shallowOcean();
        float beach = this.controlPoints.beach();
        float nearInland = this.controlPoints.nearInland();
        float midInland = this.controlPoints.midInland();
        float farInland = this.controlPoints.farInland();

        float continentNoise = cell.continentNoise;

        if (continentNoise <= deepOcean || cell.terrain.isDeepOcean()) {
            float alpha = NoiseUtil.map(continentNoise, 0.0F, deepOcean);
            cell.continentalness = Continentalness.DEEP_OCEAN.lerp(alpha);
        } else if (continentNoise <= shallowOcean || cell.terrain.isShallowOcean()) {
            float alpha = NoiseUtil.map(continentNoise, deepOcean, shallowOcean);
            cell.continentalness = Continentalness.OCEAN.lerp(alpha);
        } else if (continentNoise <= nearInland) {
            float alpha = NoiseUtil.map(continentNoise, shallowOcean, nearInland);
            cell.continentalness = Continentalness.NEAR_INLAND.lerp(alpha);
        } else if (continentNoise <= midInland) {
            float alpha = NoiseUtil.map(continentNoise, nearInland, midInland);
            cell.continentalness = NoiseUtil.lerp(Continentalness.MID_INLAND.min(), Continentalness.MID_INLAND.max(), alpha);
        } else {
            float alpha = NoiseUtil.map(continentNoise, midInland, farInland);
            alpha = Math.min(alpha, 1.0F);
            cell.continentalness = NoiseUtil.lerp(Continentalness.FAR_INLAND.min(), Continentalness.FAR_INLAND.max(), alpha);
        }

        if (cell.terrain.getDelegate() == TerrainCategory.BEACH && cell.height + this.beachAlpha.compute(x, z, 0) < this.levels.water(5)) {
            float alpha = NoiseUtil.clamp(cell.continentEdge, shallowOcean, beach);
            alpha = NoiseUtil.lerp(alpha, shallowOcean, beach, 0.0F, 1.0F);
            cell.continentalness = NoiseUtil.lerp(Continentalness.COAST.min(), Continentalness.COAST.max(), alpha);
        }
    }

    /**
     * 创建高度图
     */
    public static Heightmap make(GeneratorContext ctx) {
        // 控制点
        ControlPoints controlPoints = new ControlPoints(
            0.1F,   // deepOcean
            0.25F,  // shallowOcean
            0.35F,  // beach
            0.38F,  // coast
            0.45F,  // nearInland
            0.6F,   // midInland
            0.8F    // farInland
        );

        // 地形设置
        float globalVerticalScale = 1.0F;
        float globalHorizontalScale = 300.0F;
        int terrainRegionSize = 1200;
        boolean fancyMountains = true;

        // 区域变形参数
        Seed regionWarp = ctx.seed.offset(8934);
        int regionWarpScale = 400;
        int regionWarpStrength = 200;

        // 创建区域配置
        RegionConfig regionConfig = new RegionConfig(
            ctx.seed.root() + 789124,
            terrainRegionSize,
            Noises.simplex(regionWarp.next(), regionWarpScale, 1),
            Noises.simplex(regionWarp.next(), regionWarpScale, 1),
            regionWarpStrength
        );

        Levels levels = ctx.levels;
        float terrainFrequency = 1.0F / globalHorizontalScale;
        CellPopulator region = new RegionModule(regionConfig);

        // 山脉链噪声
        Seed mountainSeed = ctx.seed.offset(0);
        Noise mountainChainAlpha = Noises.worleyEdge(mountainSeed.next(), 1000, EdgeFunction.DISTANCE_2_ADD, DistanceFunction.EUCLIDEAN);
        mountainChainAlpha = Noises.warpPerlin(mountainChainAlpha, mountainSeed.next(), 333, 2, 250.0F);
        mountainChainAlpha = Noises.curve(mountainChainAlpha, Interpolation.CURVE3);
        mountainChainAlpha = Noises.clamp(mountainChainAlpha, 0.0F, 0.9F);
        mountainChainAlpha = Noises.map(mountainChainAlpha, 0.0F, 1.0F);

        // 地面高度噪声
        int groundVariance = 25;
        Noise ground = Noises.cell(CellField.CONTINENT_NOISE);
        ground = Noises.clamp(ground, controlPoints.coast(), controlPoints.farInland());
        ground = Noises.map(ground, 0.0F, 1.0F);
        ground = Noises.mul(ground, levels.scale(groundVariance));
        ground = Noises.add(ground, levels.ground);

        // 生成地形区域
        CellPopulator terrainRegions = new RegionSelector(TerrainProvider.generateTerrain(ctx.seed, regionConfig, levels, ground));
        CellPopulator terrainRegionBorders = Populators.makePlains(ctx.seed, ground, globalVerticalScale);

        // 地形混合
        CellPopulator terrainBlend = new RegionLerper(terrainRegionBorders, terrainRegions);
        CellPopulator mountains = Populators.makeMountainChain(mountainSeed, ground, globalVerticalScale, 1.0F);
        
        // 创建大陆
        Continent continent = ctx.settings.continentType.create(ctx.seed, ctx);
        
        Climate climate = Climate.make(continent, ctx);
        CellPopulator land = new MountainChainPopulator(terrainBlend, mountains, 0.3F, 0.8F);

        // 海洋地形
        CellPopulator deepOcean = Populators.makeDeepOcean(ctx.seed.next(), levels.water);
        CellPopulator shallowOcean = Populators.makeShallowOcean(levels);
        CellPopulator coast = Populators.makeCoast(levels);

        // 大陆插值
        CellPopulator oceans = new ContinentLerper3(deepOcean, shallowOcean, coast, controlPoints.deepOcean(), controlPoints.shallowOcean(), controlPoints.coast());
        CellPopulator terrain = new ContinentLerper2(oceans, land, controlPoints.shallowOcean(), controlPoints.nearInland());

        // 海滩噪声
        Noise beachNoise = Noises.perlin2(ctx.seed.next(), 20, 1);
        beachNoise = Noises.mul(beachNoise, ctx.levels.scale(5));

        CellSamplerProvider cellProvider = new CellSamplerProvider();
        return new Heightmap(cellProvider, terrain.mapNoise(cellProvider), region, continent, climate, levels, controlPoints, terrainFrequency, mountainChainAlpha, beachNoise);
    }
}
