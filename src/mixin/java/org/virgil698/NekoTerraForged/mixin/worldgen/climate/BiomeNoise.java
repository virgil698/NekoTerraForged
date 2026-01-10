package org.virgil698.NekoTerraForged.mixin.worldgen.climate;

import org.virgil698.NekoTerraForged.mixin.worldgen.biome.type.BiomeType;
import org.virgil698.NekoTerraForged.mixin.worldgen.cell.Cell;
import org.virgil698.NekoTerraForged.mixin.worldgen.continent.Continent;
import org.virgil698.NekoTerraForged.mixin.worldgen.heightmap.Levels;
import org.virgil698.NekoTerraForged.mixin.worldgen.noise.NoiseUtil;
import org.virgil698.NekoTerraForged.mixin.worldgen.noise.function.DistanceFunction;
import org.virgil698.NekoTerraForged.mixin.worldgen.noise.function.EdgeFunction;
import org.virgil698.NekoTerraForged.mixin.worldgen.noise.module.Noise;
import org.virgil698.NekoTerraForged.mixin.worldgen.noise.module.Noises;
import org.virgil698.NekoTerraForged.mixin.worldgen.terrain.TerrainType;
import org.virgil698.NekoTerraForged.mixin.worldgen.util.Seed;

/**
 * 生物群系噪声
 * 移植自 ReTerraForged
 */
public class BiomeNoise {
    private final int seed;
    private final float biomeFreq;
    private final float warpStrength;
    private final Noise warpX;
    private final Noise warpZ;
    private final Noise moisture;
    private final Noise temperature;
    private final Noise macroBiomeNoise;
    private final Continent continent;
    private final Levels levels;

    // 控制点
    private final float coastMarker = 0.35F;

    public BiomeNoise(Seed seed, Continent continent, Levels levels) {
        int biomeSize = 250;
        int warpScale = 150;

        this.continent = continent;
        this.seed = seed.next();
        this.biomeFreq = 1.0F / biomeSize;
        this.warpStrength = 80.0F;
        this.levels = levels;

        // 变形噪声
        Noise warpX = Noises.simplex(seed.next(), warpScale, 2);
        warpX = Noises.add(warpX, -0.5F);
        this.warpX = warpX;

        Noise warpZ = Noises.simplex(seed.next(), warpScale, 2);
        warpZ = Noises.add(warpZ, -0.5F);
        this.warpZ = warpZ;

        // 湿度噪声
        int moistScale = 8;
        Noise moistureSource = Noises.simplex(seed.next(), moistScale, 1);
        moistureSource = Noises.clamp(moistureSource, 0.125F, 0.875F);
        moistureSource = Noises.map(moistureSource, 0.0F, 1.0F);
        this.moisture = moistureSource;

        // 温度噪声
        int tempScale = 6;
        Noise temperatureNoise = Noises.simplex(seed.next(), tempScale, 2);
        temperatureNoise = Noises.map(temperatureNoise, 0.0F, 1.0F);
        this.temperature = temperatureNoise;

        // 宏观生物群系噪声
        this.macroBiomeNoise = Noises.worley(seed.next(), 200);
    }

    public void apply(Cell cell, float x, float z, float originalX, float originalZ) {
        this.apply(cell, x, z, originalX, originalZ, true);
    }

    public void apply(Cell cell, float x, float z, float originalX, float originalZ, boolean mask) {
        float ox = this.warpX.compute(x, z, 0) * this.warpStrength;
        float oz = this.warpZ.compute(x, z, 0) * this.warpStrength;
        x += ox;
        z += oz;
        x *= this.biomeFreq;
        z *= this.biomeFreq;

        int xr = NoiseUtil.floor(x);
        int zr = NoiseUtil.floor(z);
        int cellX = xr;
        int cellZ = zr;
        float centerX = x;
        float centerZ = z;
        float edgeDistance = 999999.0F;
        float edgeDistance2 = 999999.0F;
        DistanceFunction dist = DistanceFunction.EUCLIDEAN;

        for (int dz = -1; dz <= 1; ++dz) {
            for (int dx = -1; dx <= 1; ++dx) {
                int cx = xr + dx;
                int cz = zr + dz;
                NoiseUtil.Vec2f vec = NoiseUtil.cell(this.seed, cx, cz);
                float cxf = cx + vec.x();
                float czf = cz + vec.y();
                float distance = dist.apply(cxf - x, czf - z);
                if (distance < edgeDistance) {
                    edgeDistance2 = edgeDistance;
                    edgeDistance = distance;
                    centerX = cxf;
                    centerZ = czf;
                    cellX = cx;
                    cellZ = cz;
                } else if (distance < edgeDistance2) {
                    edgeDistance2 = distance;
                }
            }
        }

        cell.biomeRegionId = cellValue(this.seed, cellX, cellZ);
        cell.regionMoisture = this.moisture.compute(centerX, centerZ, 0);
        cell.regionTemperature = this.temperature.compute(centerX, centerZ, 0);
        cell.macroBiomeId = this.macroBiomeNoise.compute(centerX, centerZ, 0);

        int posX = NoiseUtil.floor(centerX / this.biomeFreq);
        int posZ = NoiseUtil.floor(centerZ / this.biomeFreq);
        float continentEdge = this.continent.getLandValue(posX, posZ);

        if (mask) {
            cell.biomeRegionEdge = edgeValue(edgeDistance, edgeDistance2);
            this.modifyTerrain(cell, continentEdge);
        }

        cell.regionMoisture = this.modifyMoisture(cell.regionMoisture, continentEdge);
        cell.biomeType = BiomeType.get(cell.regionTemperature, cell.regionMoisture);
        cell.regionTemperature = this.modifyTemp(cell.height, cell.regionTemperature, originalX, originalZ);

        cell.temperature = cell.biomeType.getTemperature(cell.biomeRegionId);
        cell.moisture = cell.biomeType.getMoisture(cell.biomeRegionId);
    }

    private float modifyTemp(float height, float temp, float x, float z) {
        if (height > 0.75F) {
            return Math.max(0.0F, temp - 0.05F);
        }
        if (height > 0.45F) {
            float delta = (height - 0.45F) / 0.3F;
            return Math.max(0.0F, temp - delta * 0.05F);
        }
        height = Math.max(this.levels.ground, height);
        if (height >= this.levels.ground) {
            float delta = 1.0F - (height - this.levels.ground) / (0.45F - this.levels.ground);
            return Math.min(1.0F, temp + delta * 0.05F);
        }
        return temp;
    }

    private float modifyMoisture(float moisture, float continentEdge) {
        float limit = 0.75F;
        float range = 1.0F - limit;
        if (continentEdge < limit) {
            float alpha = (limit - continentEdge) / range;
            float multiplier = 1.0F + alpha * range;
            return NoiseUtil.clamp(moisture * multiplier, 0.0F, 1.0F);
        } else {
            float alpha = (continentEdge - limit) / range;
            float multiplier = 1.0F - alpha * range;
            return moisture * multiplier;
        }
    }

    private void modifyTerrain(Cell cell, float continentEdge) {
        if (cell.terrain.isOverground() && !cell.terrain.overridesCoast() && continentEdge <= this.coastMarker) {
            cell.terrain = TerrainType.COAST;
        }
    }

    private static float cellValue(int seed, int cellX, int cellY) {
        float value = NoiseUtil.valCoord2D(seed, cellX, cellY);
        return NoiseUtil.map(value, -1.0F, 1.0F, 2.0F);
    }

    private static float edgeValue(float distance, float distance2) {
        EdgeFunction edge = EdgeFunction.DISTANCE_2_DIV;
        float value = edge.apply(distance, distance2);
        value = 1.0F - NoiseUtil.map(value, edge.min(), edge.max(), edge.range());
        return value;
    }
}
