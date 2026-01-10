package org.virgil698.NekoTerraForged.mixin.worldgen.continent;

import org.virgil698.NekoTerraForged.mixin.worldgen.GeneratorContext;
import org.virgil698.NekoTerraForged.mixin.worldgen.cell.Cell;
import org.virgil698.NekoTerraForged.mixin.worldgen.cell.CellPopulator;
import org.virgil698.NekoTerraForged.mixin.worldgen.noise.NoiseUtil;
import org.virgil698.NekoTerraForged.mixin.worldgen.noise.NoiseUtil.Vec2f;
import org.virgil698.NekoTerraForged.mixin.worldgen.noise.domain.Domain;
import org.virgil698.NekoTerraForged.mixin.worldgen.noise.domain.Domains;
import org.virgil698.NekoTerraForged.mixin.worldgen.noise.function.DistanceFunction;
import org.virgil698.NekoTerraForged.mixin.worldgen.noise.function.EdgeFunction;
import org.virgil698.NekoTerraForged.mixin.worldgen.noise.module.Noise;
import org.virgil698.NekoTerraForged.mixin.worldgen.noise.module.Noises;
import org.virgil698.NekoTerraForged.mixin.worldgen.rivermap.LegacyRiverCache;
import org.virgil698.NekoTerraForged.mixin.worldgen.rivermap.RiverCache;
import org.virgil698.NekoTerraForged.mixin.worldgen.rivermap.Rivermap;
import org.virgil698.NekoTerraForged.mixin.worldgen.util.PosUtil;
import org.virgil698.NekoTerraForged.mixin.worldgen.util.Seed;

/**
 * 简单大陆生成器
 * 移植自 ReTerraForged
 */
public class SimpleContinentGenerator implements SimpleContinent {
    protected int seed;
    protected float frequency;
    protected int continentScale;
    private DistanceFunction distanceFunc;
    private float clampMin;
    private float clampMax;
    private float clampRange;
    private float offsetAlpha;
    protected Domain warp;
    protected Noise continentalness;
    protected RiverCache cache;

    // 控制点
    private float deepOcean;
    private float shallowOcean;
    private float nearInland;

    public SimpleContinentGenerator(Seed seed, GeneratorContext context) {
        int tectonicScale = 3000 * 4;
        this.continentScale = 3000 / 2;
        this.seed = seed.next();
        this.distanceFunc = DistanceFunction.EUCLIDEAN;
        this.frequency = 1.0F / tectonicScale;
        this.clampMin = 0.2F;
        this.clampMax = 1.0F;
        this.clampRange = this.clampMax - this.clampMin;
        this.offsetAlpha = 0.7F;

        // 控制点
        this.deepOcean = 0.1F;
        this.shallowOcean = 0.25F;
        this.nearInland = 0.45F;

        Domain warp = Domains.domainPerlin(seed.next(), 20, 2, 20.0F);
        warp = Domains.compound(warp, Domains.domainSimplex(seed.next(), this.continentScale, 3, this.continentScale));
        this.warp = warp;

        Noise shape = Noises.simplex(seed.next(), 3000 * 2, 1);
        shape = Noises.add(shape, 0.65F);
        shape = Noises.clamp(shape, 0.0F, 1.0F);
        this.continentalness = shape;

        this.cache = new LegacyRiverCache(new SimpleRiverGenerator(this, context));
    }

    @Override
    public Rivermap getRivermap(int x, int y) {
        return this.cache.getRivers(x, y);
    }


    @Override
    public void apply(Cell cell, float x, float y) {
        float ox = this.warp.getOffsetX(x, y, 0);
        float oz = this.warp.getOffsetZ(x, y, 0);
        float px = x + ox;
        float py = y + oz;
        px *= this.frequency;
        py *= this.frequency;
        int xr = NoiseUtil.floor(px);
        int yr = NoiseUtil.floor(py);
        int cellX = xr;
        int cellY = yr;
        float centerX = px;
        float centerY = py;
        float edgeDistance = 999999.0F;
        float edgeDistance2 = 999999.0F;
        for (int dy = -1; dy <= 1; ++dy) {
            for (int dx = -1; dx <= 1; ++dx) {
                int cx = xr + dx;
                int cy = yr + dy;
                Vec2f vec = NoiseUtil.cell(this.seed, cx, cy);
                float cxf = cx + vec.x() * this.offsetAlpha;
                float cyf = cy + vec.y() * this.offsetAlpha;
                float distance = this.distanceFunc.apply(cxf - px, cyf - py);
                if (distance < edgeDistance) {
                    edgeDistance2 = edgeDistance;
                    edgeDistance = distance;
                    centerX = cxf;
                    centerY = cyf;
                    cellX = cx;
                    cellY = cy;
                } else if (distance < edgeDistance2) {
                    edgeDistance2 = distance;
                }
            }
        }
        cell.continentId = this.cellIdentity(this.seed, cellX, cellY);
        float continentalness = this.cellEdgeValue(edgeDistance, edgeDistance2);
        cell.continentX = (int) (centerX / this.frequency);
        cell.continentZ = (int) (centerY / this.frequency);
        continentalness *= this.getContinentalness(x, y, continentalness);
        cell.continentEdge = continentalness;
        cell.continentNoise = continentalness;
    }

    @Override
    public float getEdgeValue(float x, float y) {
        float ox = this.warp.getOffsetX(x, y, 0);
        float oz = this.warp.getOffsetZ(x, y, 0);
        float px = x + ox;
        float py = y + oz;
        px *= this.frequency;
        py *= this.frequency;
        int xr = NoiseUtil.floor(px);
        int yr = NoiseUtil.floor(py);
        float edgeDistance = 999999.0F;
        float edgeDistance2 = 999999.0F;
        for (int dy = -1; dy <= 1; ++dy) {
            for (int dx = -1; dx <= 1; ++dx) {
                int cx = xr + dx;
                int cy = yr + dy;
                Vec2f vec = NoiseUtil.cell(this.seed, cx, cy);
                float cxf = cx + vec.x() * this.offsetAlpha;
                float cyf = cy + vec.y() * this.offsetAlpha;
                float distance = this.distanceFunc.apply(cxf - px, cyf - py);
                if (distance < edgeDistance) {
                    edgeDistance2 = edgeDistance;
                    edgeDistance = distance;
                } else if (distance < edgeDistance2) {
                    edgeDistance2 = distance;
                }
            }
        }
        float edgeValue = this.cellEdgeValue(edgeDistance, edgeDistance2);
        float shapeNoise = this.getContinentalness(x, y, edgeValue);
        return edgeValue * shapeNoise;
    }

    @Override
    public long getNearestCenter(float x, float z) {
        float ox = this.warp.getOffsetX(x, z, 0);
        float oz = this.warp.getOffsetZ(x, z, 0);
        float px = x + ox;
        float py = z + oz;
        px *= this.frequency;
        py *= this.frequency;
        float centerX = px;
        float centerY = py;
        int xr = NoiseUtil.floor(px);
        int yr = NoiseUtil.floor(py);
        float edgeDistance = 999999.0F;
        for (int dy = -1; dy <= 1; ++dy) {
            for (int dx = -1; dx <= 1; ++dx) {
                int cx = xr + dx;
                int cy = yr + dy;
                Vec2f vec = NoiseUtil.cell(this.seed, cx, cy);
                float cxf = cx + vec.x() * this.offsetAlpha;
                float cyf = cy + vec.y() * this.offsetAlpha;
                float distance = this.distanceFunc.apply(cxf - px, cyf - py);
                if (distance < edgeDistance) {
                    edgeDistance = distance;
                    centerX = cxf;
                    centerY = cyf;
                }
            }
        }
        int conX = (int) (centerX / this.frequency);
        int conZ = (int) (centerY / this.frequency);
        return PosUtil.pack(conX, conZ);
    }


    @Override
    public float getDistanceToOcean(int cx, int cz, float dx, float dz) {
        float high = this.getDistanceToEdge(cx, cz, dx, dz);
        float low = 0.0F;
        for (int i = 0; i < 50; ++i) {
            float mid = (low + high) / 2.0F;
            float x = cx + dx * mid;
            float z = cz + dz * mid;
            float edge = this.getEdgeValue(x, z);
            if (edge > this.shallowOcean) {
                low = mid;
            } else {
                high = mid;
            }
            if (high - low < 10.0f) {
                break;
            }
        }
        return high;
    }

    @Override
    public float getDistanceToEdge(int cx, int cz, float dx, float dz) {
        float distance = (float) (this.continentScale * 4);
        for (int i = 0; i < 10; ++i) {
            float x = cx + dx * distance;
            float z = cz + dz * distance;
            long centerPos = this.getNearestCenter(x, z);
            int conX = PosUtil.unpackLeft(centerPos);
            int conZ = PosUtil.unpackRight(centerPos);
            distance += distance;
            if (conX != cx || conZ != cz) {
                float low = 0.0F;
                float high = distance;
                for (int j = 0; j < 50; ++j) {
                    float mid = (low + high) / 2.0F;
                    float px = cx + dx * mid;
                    float pz = cz + dz * mid;
                    centerPos = this.getNearestCenter(px, pz);
                    conX = PosUtil.unpackLeft(centerPos);
                    conZ = PosUtil.unpackRight(centerPos);
                    if (conX == cx && conZ == cz) {
                        low = mid;
                    } else {
                        high = mid;
                    }
                    if (high - low < 50.0F) {
                        break;
                    }
                }
                return high;
            }
        }
        return distance;
    }

    protected float cellIdentity(int seed, int cellX, int cellY) {
        float value = NoiseUtil.valCoord2D(seed, cellX, cellY);
        return NoiseUtil.map(value, -1.0F, 1.0F, 2.0F);
    }

    protected float cellEdgeValue(float distance, float distance2) {
        EdgeFunction edge = EdgeFunction.DISTANCE_2_DIV;
        float value = edge.apply(distance, distance2);
        value = 1.0F - NoiseUtil.map(value, edge.min(), edge.max(), edge.range());
        if (value <= this.clampMin) {
            return 0.0F;
        }
        if (value >= this.clampMax) {
            return 1.0F;
        }
        return (value - this.clampMin) / this.clampRange;
    }

    protected float getContinentalness(float x, float z, float edgeValue) {
        if (edgeValue >= this.nearInland) {
            return 1.0F;
        }
        float alpha = edgeValue / this.nearInland;
        return this.continentalness.compute(x, z, 0) * alpha;
    }

    @Override
    public CellPopulator mapNoise(Noise.Visitor visitor) {
        return this;
    }
}
