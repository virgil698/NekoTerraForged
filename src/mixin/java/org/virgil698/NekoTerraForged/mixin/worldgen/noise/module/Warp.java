package org.virgil698.NekoTerraForged.mixin.worldgen.noise.module;

import org.virgil698.NekoTerraForged.mixin.worldgen.noise.domain.Domain;

/**
 * 变形噪声
 * 移植自 ReTerraForged
 */
public class Warp implements Noise {
    private final Noise input;
    private final Domain domain;

    public Warp(Noise input, Domain domain) {
        this.input = input;
        this.domain = domain;
    }

    public Noise input() {
        return input;
    }

    public Domain domain() {
        return domain;
    }

    @Override
    public float compute(float x, float z, int seed) {
        return this.input.compute(this.domain.getX(x, z, seed), this.domain.getZ(x, z, seed), seed);
    }

    @Override
    public float minValue() {
        return this.input.minValue();
    }

    @Override
    public float maxValue() {
        return this.input.maxValue();
    }

    @Override
    public Noise mapAll(Visitor visitor) {
        return visitor.apply(new Warp(this.input.mapAll(visitor), this.domain.mapAll(visitor)));
    }
}
