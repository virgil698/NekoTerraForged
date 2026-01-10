package org.virgil698.NekoTerraForged.mixin.worldgen.noise.domain;

import org.virgil698.NekoTerraForged.mixin.worldgen.noise.module.Noise.Visitor;

/**
 * 加法域变形
 * 移植自 ReTerraForged
 */
public record AddWarp(Domain input1, Domain input2) implements Domain {

    @Override
    public float getOffsetX(float x, float z, int seed) {
        return this.input1.getOffsetX(x, z, seed) + this.input2.getOffsetX(x, z, seed);
    }

    @Override
    public float getOffsetZ(float x, float z, int seed) {
        return this.input1.getOffsetZ(x, z, seed) + this.input2.getOffsetZ(x, z, seed);
    }

    @Override
    public Domain mapAll(Visitor visitor) {
        return new AddWarp(this.input1.mapAll(visitor), this.input2.mapAll(visitor));
    }
}
