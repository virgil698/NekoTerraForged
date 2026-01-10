package org.virgil698.NekoTerraForged.mixin.worldgen.util;

/**
 * 浮点数边界
 * 移植自 ReTerraForged
 */
public record Boundsf(float minX, float minZ, float maxX, float maxZ) {
    public static final Boundsf NONE = new Boundsf(Float.MAX_VALUE, Float.MAX_VALUE, Float.MIN_VALUE, Float.MIN_VALUE);

    public boolean contains(float x, float z) {
        return x >= minX && x <= maxX && z >= minZ && z <= maxZ;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private float minX = Float.MAX_VALUE;
        private float minZ = Float.MAX_VALUE;
        private float maxX = Float.MIN_VALUE;
        private float maxZ = Float.MIN_VALUE;

        public Builder record(float x, float z) {
            this.minX = Math.min(this.minX, x);
            this.minZ = Math.min(this.minZ, z);
            this.maxX = Math.max(this.maxX, x);
            this.maxZ = Math.max(this.maxZ, z);
            return this;
        }

        public Boundsf build() {
            return new Boundsf(minX, minZ, maxX, maxZ);
        }
    }
}
