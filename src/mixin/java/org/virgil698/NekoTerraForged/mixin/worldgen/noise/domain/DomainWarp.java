package org.virgil698.NekoTerraForged.mixin.worldgen.noise.domain;

import org.virgil698.NekoTerraForged.mixin.worldgen.noise.module.Noise;
import org.virgil698.NekoTerraForged.mixin.worldgen.noise.module.Noises;

/**
 * 域变形实现
 * 移植自 ReTerraForged
 */
public class DomainWarp implements Domain {
    private final Noise x;
    private final Noise z;
    private final Noise mappedX;
    private final Noise mappedZ;
    private final Noise distance;

    public DomainWarp(Noise x, Noise z, Noise distance) {
        this(x, z, map(x), map(z), distance);
    }

    private DomainWarp(Noise x, Noise z, Noise mappedX, Noise mappedZ, Noise distance) {
        this.x = x;
        this.z = z;
        this.mappedX = mappedX;
        this.mappedZ = mappedZ;
        this.distance = distance;
    }

    public Noise x() {
        return x;
    }

    public Noise z() {
        return z;
    }

    public Noise distance() {
        return distance;
    }

    @Override
    public float getOffsetX(float x, float z, int seed) {
        return this.mappedX.compute(x, z, seed) * this.distance.compute(x, z, seed);
    }

    @Override
    public float getOffsetZ(float x, float z, int seed) {
        return this.mappedZ.compute(x, z, seed) * this.distance.compute(x, z, seed);
    }

    @Override
    public Domain mapAll(Noise.Visitor visitor) {
        return new DomainWarp(
            this.x.mapAll(visitor),
            this.z.mapAll(visitor),
            this.mappedX.mapAll(visitor),
            this.mappedZ.mapAll(visitor),
            this.distance.mapAll(visitor)
        );
    }

    private static Noise map(Noise in) {
        if (in.minValue() == -0.5F && in.maxValue() == 0.5F) {
            return in;
        }
        return Noises.map(in, -0.5F, 0.5F);
    }
}
