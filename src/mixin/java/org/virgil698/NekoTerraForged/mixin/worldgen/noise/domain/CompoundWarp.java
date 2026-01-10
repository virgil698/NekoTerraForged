package org.virgil698.NekoTerraForged.mixin.worldgen.noise.domain;

import org.virgil698.NekoTerraForged.mixin.worldgen.noise.module.Noise.Visitor;

/**
 * 复合域变形
 * 移植自 ReTerraForged
 */
public record CompoundWarp(Domain input1, Domain input2) implements Domain {

    @Override
    public float getOffsetX(float x, float z, int seed) {
        float ax = this.input1.getX(x, z, seed);
        float ay = this.input1.getZ(x, z, seed);
        return this.input2.getOffsetX(ax, ay, seed);
    }

    @Override
    public float getOffsetZ(float x, float z, int seed) {
        float ax = this.input1.getX(x, z, seed);
        float ay = this.input1.getZ(x, z, seed);
        return this.input2.getOffsetZ(ax, ay, seed);
    }

    @Override
    public Domain mapAll(Visitor visitor) {
        return new CompoundWarp(this.input1.mapAll(visitor), this.input2.mapAll(visitor));
    }
}
