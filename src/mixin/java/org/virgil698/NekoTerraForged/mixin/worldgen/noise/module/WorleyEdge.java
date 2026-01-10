package org.virgil698.NekoTerraForged.mixin.worldgen.noise.module;

import org.virgil698.NekoTerraForged.mixin.worldgen.noise.NoiseUtil;
import org.virgil698.NekoTerraForged.mixin.worldgen.noise.function.DistanceFunction;
import org.virgil698.NekoTerraForged.mixin.worldgen.noise.function.EdgeFunction;

/**
 * Worley 边缘噪声
 * 移植自 ReTerraForged
 */
public class WorleyEdge implements Noise {
    private final float frequency;
    private final float distance;
    private final EdgeFunction edgeFunction;
    private final DistanceFunction distanceFunction;

    public WorleyEdge(float frequency, float distance, EdgeFunction edgeFunction, DistanceFunction distanceFunction) {
        this.frequency = frequency;
        this.distance = distance;
        this.edgeFunction = edgeFunction;
        this.distanceFunction = distanceFunction;
    }

    public float frequency() { return frequency; }
    public float distance() { return distance; }
    public EdgeFunction edgeFunction() { return edgeFunction; }
    public DistanceFunction distanceFunction() { return distanceFunction; }

    @Override
    public float compute(float x, float z, int seed) {
        x *= this.frequency;
        z *= this.frequency;
        float value = sample(x, z, seed, this.distance, this.edgeFunction, this.distanceFunction);
        return NoiseUtil.map(value, this.edgeFunction.min(), this.edgeFunction.max(), this.edgeFunction.range());
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

    public static float sample(float x, float y, int seed, float distance, EdgeFunction edgeFunction, DistanceFunction distanceFunc) {
        int xi = NoiseUtil.floor(x);
        int yi = NoiseUtil.floor(y);
        float nearest1 = Float.MAX_VALUE;
        float nearest2 = Float.MAX_VALUE;
        for (int dy = -1; dy <= 1; ++dy) {
            for (int dx = -1; dx <= 1; ++dx) {
                int cx = xi + dx;
                int cy = yi + dy;
                NoiseUtil.Vec2f vec = NoiseUtil.cell(seed, cx, cy);
                float deltaX = cx + vec.x() * distance - x;
                float deltaY = cy + vec.y() * distance - y;
                float dist = distanceFunc.apply(deltaX, deltaY);
                if (dist < nearest1) {
                    nearest2 = nearest1;
                    nearest1 = dist;
                } else if (dist < nearest2) {
                    nearest2 = dist;
                }
            }
        }
        return edgeFunction.apply(nearest1, nearest2);
    }
}
